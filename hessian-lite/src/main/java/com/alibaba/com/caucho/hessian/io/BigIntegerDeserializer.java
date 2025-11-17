/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.com.caucho.hessian.io;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Deserializing a BigInteger
 */
public class BigIntegerDeserializer extends AbstractDeserializer {

    @Override
    public Class<?> getType() {
        return BigInteger.class;
    }

    @Override
    public Object readObject(AbstractHessianInput in,
                             Object[] fieldNames)
            throws IOException {

        int i = in.addRef(null);

        Integer signum = null;
        byte[] magnitude = null;
        for (Object fieldName : fieldNames) {
            if ("signum".equals(fieldName)) {
                signum = in.readInt();
            } else if ("mag".equals(fieldName)) {
                magnitude = magSerializedForm((int[])in.readObject());
            } else {
                in.readObject();
            }
        }

        if (signum == null || magnitude == null) {
            throw new IOException("Missing required fields for BigInteger deserialization: "
                    + "signum=" + signum + ", mag=" + Arrays.toString(magnitude));
        }

        BigInteger bigInteger = new BigInteger(signum, magnitude);
        in.setRef(i, bigInteger);
        return bigInteger;
    }

    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    /**
     * copy from BigInteger#magSerializedForm()
     */
    private byte[] magSerializedForm(int mag[]) {
        int len = mag.length;

        int bitLen = (len == 0 ? 0 : ((len - 1) << 5) + bitLengthForInt(mag[0]));
        int byteLen = (bitLen + 7) >>> 3;
        byte[] result = new byte[byteLen];

        for (int i = byteLen - 1, bytesCopied = 4, intIndex = len - 1, nextInt = 0;
             i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = mag[intIndex--];
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            result[i] = (byte)nextInt;
        }
        return result;
    }
}

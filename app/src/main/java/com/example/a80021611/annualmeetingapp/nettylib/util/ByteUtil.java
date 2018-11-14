package com.example.a80021611.annualmeetingapp.nettylib.util;

import com.example.a80021611.annualmeetingapp.nettybussiness.message.Request0x7A;

import java.io.IOException;
import java.util.Arrays;

/**
 * <p>描述：(这里用一句话描述这个类的作用)</p>
 * 作者： wood121<br>
 * 日期： 2018/11/8 19:04<br>
 * 版本： v2.0<br>
 */
public class ByteUtil {
    /**
     * 两个byte 数组叠加. 将 desBytes 添加到 srcBytes
     *
     * @param srcBytes 被添加的byte s数组
     * @param desBytes 　添加的byte 数组
     * @return byte[]　返回添加后的数组
     */
    public static byte[] addBytes(byte[] srcBytes, byte[] desBytes) {
        if (!notNull(srcBytes) && !notNull(desBytes)) {
            // no array to add
            return null;
        }
        if (notNull(srcBytes) && !notNull(desBytes)) {
            return srcBytes;
        }

        if (!notNull(srcBytes) && notNull(desBytes)) {
            return desBytes;
        }
        // copy array
        byte[] returnArray = new byte[srcBytes.length + desBytes.length];
        System.arraycopy(srcBytes, 0, returnArray, 0, srcBytes.length);
        System.arraycopy(desBytes, 0, returnArray, srcBytes.length, desBytes.length);
        return returnArray;
    }

    /**
     * 将 一个desByte 添加到 srcBytes
     *
     * @param srcBytes 　被添加的byte 数组
     * @param desByte  　添加的byte 数组
     * @return byte[]　返回添加后的数组
     */
    public static byte[] addBytes(byte[] srcBytes, byte desByte) {
        byte[] desByteArray = new byte[]{desByte};
        return addBytes(srcBytes, desByteArray);
    }

    /**
     * 将一个byte 插入到 byte数组中
     *
     * @param srcBytes 　被插入的byte 数组
     * @param desByte  　插入的byte
     * @param index    建议 index 小于等于srcBytes 的长度，如果大于，那么直接在后面添加
     * @return byte[]　返回被插入后的数组
     */
    public static byte[] insertByte(byte[] srcBytes, byte desByte, int index) {
        if (!notNull(srcBytes)) {
            //此时表示源数组为null，那么直接创建一个数组并且返回
            return new byte[]{desByte};
        }
        byte[] desByteArray = new byte[]{desByte};
        int srcLength = srcBytes.length;
        if (srcLength <= index) {
            //直接后面插入
            return addBytes(srcBytes, desByteArray);
        } else {
            // copy array
            byte[] returnArray = new byte[srcBytes.length + 1];
            System.arraycopy(srcBytes, 0, returnArray, 0, index);
            System.arraycopy(desByteArray, 0, returnArray, index, desByteArray.length);
            System.arraycopy(srcBytes, index, returnArray, index + 1, srcLength - index);
            return returnArray;
        }
    }

    /**
     * 将一个 desBytes 数组 插入到 srcBytes数组中
     *
     * @param srcBytes 　被插入的byte 数组
     * @param desBytes 　插入的byte数组
     * @param index    建议 index 小于等于srcBytes 的长度，如果大于，那么直接在后面添加
     * @return byte[]　返回被插入后的数组
     */
    public static byte[] insertBytes(byte[] srcBytes, byte[] desBytes, int index) {
        if (!notNull(srcBytes) && !notNull(desBytes)) {
            // no array to add
            return null;
        }
        if (notNull(srcBytes) && !notNull(desBytes)) {
            return srcBytes;
        }

        if (!notNull(srcBytes) && notNull(desBytes)) {
            return desBytes;
        }
        int srcLength = srcBytes.length;
        if (srcLength <= index) {
            //直接后面插入
            return addBytes(srcBytes, desBytes);
        } else {
            // copy array
            byte[] returnArray = new byte[srcBytes.length + desBytes.length];
            System.arraycopy(srcBytes, 0, returnArray, 0, index);
            System.arraycopy(desBytes, 0, returnArray, index, desBytes.length);
            System.arraycopy(srcBytes, index, returnArray, index + desBytes.length, srcLength -
                    index);
            return returnArray;
        }
    }

    /**
     * 判断数组不为null 或者长度不为0
     *
     * @param bytes 　源
     * @return return true,if byte array is not null;
     */
    private static boolean notNull(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return false;
        }
        return true;
    }

    /**
     * 从byte[]中抽取新的byte[]，使用Arrays.copyOfRange()
     *
     * @param data  - 元数据
     * @param start - 开始位置
     * @param end   - 结束位置
     * @return 新byte[]
     */
    public static byte[] getByteArr(byte[] data, int start, int end) {
        byte[] ret = new byte[end - start];
        for (int i = 0; (start + i) < end; i++) {
            ret[i] = data[start + i];
        }
        return ret;
    }

    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];

        for (int i = 0; i < l; ++i) {
            ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }

        return ret;
    }

    public static String bytes2hexString(byte[] bytes) {
        final String HEX = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt(b & 0x0f));
        }
        return sb.toString();
    }

    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    public static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0);
    }

    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        int len = Math.min(b.length - offset, 4);
        for (int i = 0; i < len; i++) {
            int shift = (len - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * CRC16/X25校验
     *
     * @param data
     * @param length
     * @return
     */
    public static int computeChecksum(byte[] data, int length) {
        int j = 0;
        int crc16 = 0x0000FFFF;
        for (int i = 1; i < length; i++) {
            crc16 ^= data[i] & 0x000000FF;
            for (j = 0; j < 8; j++) {
                int flags = crc16 & 0x00000001;
                if (flags != 0) {
                    crc16 = (crc16 >> 1) ^ 0x8408;
                } else {
                    crc16 >>= 0x01;
                }
            }
        }
        return ~crc16 & 0x0000FFFF;
    }

    /**
     * 协议头	  数据长度	 协议版本	  发送序列号	是否需要回复	集群ID	发送方网关ID	接收方网关ID	命令优先级 命令类型 数据内容	 校验值
     * 0x7A	    2	    2	    4	        1	    2	        2	          2	           1	    2	    N	   2
     * eg:心跳包内容 0x7A	0x0010	0x1000	0x0001(需递增)	0x00	0x0000	0x0010	0x8000	0x00	0x1001	0x00	0xDB87
     */
    public static Request0x7A getResponseRequest(byte[] bytes) {
        if (!notNull(bytes)) {
            return null;
        }
        int length = bytes.length;
        Request0x7A request0x7A = new Request0x7A();
        byte[] b_length = Arrays.copyOfRange(bytes, 1, 3);
        request0x7A.setLength(byteArrayToInt(b_length));

        byte[] b_serialNumber = Arrays.copyOfRange(bytes, 5, 9);
        request0x7A.setSerialNumber(byteArrayToInt(b_serialNumber));

        byte[] b_isNeedResponse = Arrays.copyOfRange(bytes, 9, 10);
        request0x7A.setIsNeedResponse(byteArrayToInt(b_isNeedResponse));

        byte[] b_sendId = Arrays.copyOfRange(bytes, 12, 14);
        request0x7A.setSendId(byteArrayToInt(b_sendId));

        byte[] b_receiveId = Arrays.copyOfRange(bytes, 14, 16);
        request0x7A.setReceiveId(byteArrayToInt(b_receiveId));

        byte[] b_commandPriority = Arrays.copyOfRange(bytes, 16, 17);
        request0x7A.setCommandPriority(byteArrayToInt(b_commandPriority));

        byte[] b_command_type = Arrays.copyOfRange(bytes, 17, 19);
        request0x7A.setCommandType(byteArrayToInt(b_command_type));

        byte[] b_commandContent = Arrays.copyOfRange(bytes, 19, length - 2);
        request0x7A.setCommandContent(b_commandContent);

        byte[] b_crc = Arrays.copyOfRange(bytes, length - 2, length);
        request0x7A.setVerifyCode(byteArrayToInt(b_crc));

        byte[] toCrc = Arrays.copyOfRange(bytes, 1, length - 2);
        request0x7A.setReponseVerifyCode(computeChecksum(toCrc, toCrc.length));

        return request0x7A;
    }

    public static int getNewVerifyCode(Request0x7A request0x7A) throws IOException {
        byte[] sendMsgByte = request0x7A.getSendMsgByte();
        if (notNull(sendMsgByte)) {
            if (request0x7A.getVerifyCode() != -1) {
                byte[] toCrc = Arrays.copyOfRange(sendMsgByte, 1, sendMsgByte.length - 2);
                return computeChecksum(toCrc, toCrc.length);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}

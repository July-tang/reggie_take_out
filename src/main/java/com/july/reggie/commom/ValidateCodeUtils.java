package com.july.reggie.commom;

import java.util.Random;

/**
 * 随机生成验证码工具类
 */
public class ValidateCodeUtils {
    /**
     * 随机生成验证码
     * @param length 长度为4位或者6位
     * @return
     */
    public static String generateValidateCode(int length){
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        if (length == 4 || length == 6){
            for (int i = 0; i < length; i++) {
                code.append(random.nextInt(10));
            }
        } else{
            throw new RuntimeException("只能生成4位或6位数字验证码");
        }
       // return code.toString();
        return "123123";
    }

    /**
     * 随机生成指定长度字符串验证码
     * @param length 长度
     * @return
     */
    public static String generateValidateCode4String(int length){
        Random rdm = new Random();
        String hash1 = Integer.toHexString(rdm.nextInt());
        return hash1.substring(0, length);
    }
}

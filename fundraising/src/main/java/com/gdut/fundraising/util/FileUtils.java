package com.gdut.fundraising.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    /**
     * 创建文件夹
     *
     * @param dirName
     * @return
     */
    public static boolean createDir(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {//如果文件夹不存在
            return file.mkdir();//创建文件夹
        }
        return true;
    }

    /**
     * 创建文件
     *
     * @param pathName
     * @return
     */
    public static boolean createFile(String pathName) {
        File file = new File(pathName);
        if (!file.exists()) {
            try {
                file.createNewFile(); // 创建新文件
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 写入数据
     *
     * @param pathName
     * @param data
     * @return
     */
    public static boolean write(String pathName, String data) {
        File file = new File(pathName);
        BufferedWriter writer = null;
        try {
            if (!file.exists()) {
                file.createNewFile(); // 创建新文件
            }
            writer = new BufferedWriter(new FileWriter(pathName));
            writer.write(data);
            writer.flush();// 把缓存区内容压入文件
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * 写入数据
     *
     * @param pathName
     * @param data
     * @return
     */
    public static boolean write(String pathName, byte[] data) {
        File file = new File(pathName);
        DataOutputStream out =null;
        try {
             out = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(
                            pathName)));
            if (!file.exists()) {
                file.createNewFile(); // 创建新文件
            }
            out.write(data);
            out.flush();// 把缓存区内容压入文件
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 读取数据
     *
     * @param pathName
     * @return
     */
    public static String read(String pathName) {
        File file = new File(pathName);
        // FileReader fileReader=null;
        Reader reader = null;
        try {
            //   fileReader=new FileReader(pathName);
            reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);

            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            // fileReader.close();
            reader.close();
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 读取二进制文件数据
     *
     * @param pathName
     * @return
     */
    public static byte[] readBin(String pathName) {
        File file = new File(pathName);
        // FileReader fileReader=null;
        DataInputStream in = null;
        try {
             in = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(
                            pathName)));
            byte[] data=new byte[(int) file.length()];
            in.read(data);

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    /**
     * 获取路径下的所有文件
     *
     * @param pathName
     * @return
     */
    public static File[] getAllFileInDir(String pathName) {
        File file = new File(pathName);
        return file.listFiles();
    }

    /**
     * 获取路径下的所有文件名
     *
     * @param pathName
     * @return
     */
    public static String[] getAllFileNameInDir(String pathName) {
        File file = new File(pathName);
        return file.list();
    }

    public static String getRootFilePath() {
        //当前项目下路径
        File file = new File("");
        String filePath = null;
        try {
            filePath = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    /**
     * 删除文件
     * @param pathName
     * @return
     */
    public static boolean deleteFile(String pathName){
        File file = new File(pathName);
        if(file.exists()) {
            file.delete();
        }
        return true;
    }

    public void getLujing() throws IOException {
        //当前项目下路径
        File file = new File("");
        String filePath = file.getCanonicalPath();
        System.out.println(filePath);

        //当前项目下xml文件夹
        File file1 = new File("");
        String filePath1 = file1.getCanonicalPath() + File.separator + "xml\\";
        System.out.println(filePath1);

        //获取类加载的根路径
        File file3 = new File(this.getClass().getResource("/").getPath());
        System.out.println(file3);

        //获取当前类的所在工程路径
        File file4 = new File(this.getClass().getResource("").getPath());
        System.out.println(file4);

        //获取所有的类路径 包括jar包的路径
        System.out.println(System.getProperty("java.class.path"));
    }

    /**
     * 构建文件路径
     *
     * @param root
     * @param args
     * @return
     */
    public static String buildPath(String root, String... args) {
        String os = System.getProperty("os.name");
        StringBuilder path = new StringBuilder(root);
        for (String name : args) {
            if (os.toLowerCase().startsWith("win")) {
                //如果是windows系统用双斜杠
                path.append("\\").append(name);
            } else {
                //如果是linux macos 用/
                path.append("/").append(name);
            }
        }
        return path.toString();

    }


    /**
     * 构建文件路径
     *
     * @param root
     * @param args
     * @return
     */
    public static String buildPath(String root, String s,String[] args) {
        String os = System.getProperty("os.name");
        StringBuilder path = new StringBuilder(root);

        if (os.toLowerCase().startsWith("win")) {
            //如果是windows系统用双斜杠
            path.append("\\").append(s);
        } else {
            //如果是linux macos 用/
            path.append("/").append(s);
        }

        for (String name : args) {
            if (os.toLowerCase().startsWith("win")) {
                //如果是windows系统用双斜杠
                path.append("\\").append(name);
            } else {
                //如果是linux macos 用/
                path.append("/").append(name);
            }
        }
        return path.toString();

    }


}

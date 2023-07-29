/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.sprd.validationtools.Const;

import android.text.TextUtils;
import android.util.Log;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static int getIntFromFile(String filename) {
        File file = new File(filename);
        InputStream fIn = null;
        InputStreamReader isr = null;
        try {
            fIn = new FileInputStream(file);
            isr = new InputStreamReader(fIn,
                    Charset.defaultCharset());
            char[] inputBuffer = new char[1024];
            int q = -1;
            q = isr.read(inputBuffer);
            if (q > 0)
                return Integer.parseInt(String.valueOf(inputBuffer, 0, q)
                        .trim());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }finally{
            try {
                if(isr != null){
                    isr.close();
                }
                if(fIn != null){
                    fIn.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return -1;
    }

    public static void writeFile(String filename, String content) {
        try (FileOutputStream fos = new FileOutputStream(filename)){
            byte[] bytes = content.getBytes();
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean fileIsExists(String path) {
        try {
            if(TextUtils.isEmpty(path)) return false;
            File file = new File(path);
            Log.d(TAG, "fileIsExists path=" + path);
            if (!file.exists()) {
                Log.d(TAG, path + " fileIsExists false");
                return false;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, path + " fileIsExists true");
        return true;
    }

    public static synchronized String readFile(String path) {
        File file = new File(path);
        StringBuffer sBuffer = new StringBuffer();
        try (InputStream fIn = new FileInputStream(file);BufferedReader bReader = new BufferedReader(new InputStreamReader(fIn,Charset.defaultCharset()))){
            String str = bReader.readLine();

            while (str != null) {
                sBuffer.append(str + "\n");
                str = bReader.readLine();
            }
            return sBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Bug 1672475 Modify the data reading mode of the calibrator_data node to read
         the data in the Calibrator_data node for judgment and to store the data*/
    public static int saveFileData(String readPath, String writePath) {
        int count = 0;
        File inFile = new File(readPath);
        File outFile = new File(writePath);
        FileInputStream finS = null;
        FileOutputStream foutS = null;
        try {
            finS = new FileInputStream(inFile);
            foutS = new FileOutputStream(outFile);
            byte[] bys = new byte[512];
            int c;
            while ((c = finS.read(bys)) != -1) {
                foutS.write(bys, 0, c);
                count = c;
            }
            finS.close();
            foutS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(finS != null){
                    finS.close();
                }
                if(foutS != null){
                    foutS.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return count;
    }

    public static int readFileData(String readPath) {
        int count = 0;
        File inFile = new File(readPath);
        FileInputStream finS = null;
        try {
            finS = new FileInputStream(inFile);
            byte[] bys = new byte[512];
            int c;
            while ((c = finS.read(bys)) != -1) {
                count = c;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(finS != null){
                    finS.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return count;
    }

    public static synchronized void writeAutoTestResult(int id, int result) {
        try {
            File file = new File(Const.AUTO_TEST_PATH_NAME);
            FileOutputStream fos = new FileOutputStream(file,true);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeChar(id);
            dos.writeChar(result);
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile() {
        try {
            File file = new File(Const.AUTO_TEST_PATH_NAME);
            if (file.exists() && file.isFile()) {
                file.delete();
                Log.d(TAG, "delete file is success = " + file.delete());
            }
            file.createNewFile();
            file.setExecutable(true, true);
            file.setReadable(true, false);
            file.setWritable(true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

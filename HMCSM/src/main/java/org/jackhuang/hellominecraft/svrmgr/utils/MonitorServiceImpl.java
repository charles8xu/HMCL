/*
 * Copyright 2013 huangyuhui <huanghongxun2008@126.com>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.
 */
package org.jackhuang.hellominecraft.svrmgr.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;
import com.sun.management.OperatingSystemMXBean;
import org.jackhuang.hellominecraft.utils.StrUtils;

/**
 *
 * 获取系统信息的业务逻辑实现类.
 *
 * @author GuoHuang
 */
public class MonitorServiceImpl implements IMonitorService {

    private static final int CPUTIME = 30;
    private static final int PERCENT = 100;
    private static final int FAULTLENGTH = 10;
    private static String linuxVersion = null;

    /**
     * 获得当前的监控对象.
     *
     * @return 返回构造好的监控对象
     * @throws Exception
     * @author GuoHuang
     */
    @Override
    public MonitorInfoBean getMonitorInfoBean() throws Exception {
        int kb = 1024;
        // 可使用内存
        long totalMemory = Runtime.getRuntime().totalMemory() / kb;
        // 剩余内存
        long freeMemory = Runtime.getRuntime().freeMemory() / kb;
        // 最大可使用内存
        long maxMemory = Runtime.getRuntime().maxMemory() / kb;
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        // 操作系统
        String osName = System.getProperty("os.name");
        // 总的物理内存
        long totalMemorySize = osmxb.getTotalPhysicalMemorySize() / kb;
        // 剩余的物理内存
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize() / kb;
        // 已使用的物理内存
        long usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) / kb;
        // 获得线程总数
        ThreadGroup parentThread;
        for (parentThread = Thread.currentThread().getThreadGroup(); parentThread.getParent() != null; parentThread = parentThread.getParent());
        int totalThread = parentThread.activeCount();
        double cpuRatio = 0;
        if (osName.toLowerCase().startsWith("windows"))
            cpuRatio = this.getCpuRatioForWindows();
        else
            cpuRatio = getCpuRateForLinux();
        // 构造返回对象
        MonitorInfoBean infoBean = new MonitorInfoBean();
        infoBean.setFreeMemory(freeMemory);
        infoBean.setFreePhysicalMemorySize(freePhysicalMemorySize);
        infoBean.setMaxMemory(maxMemory);
        infoBean.setOsName(osName);
        infoBean.setTotalMemory(totalMemory);
        infoBean.setTotalMemorySize(totalMemorySize);
        infoBean.setTotalThread(totalThread);
        infoBean.setUsedMemory(usedMemory);
        infoBean.setCpuRatio(cpuRatio);
        return infoBean;
    }

    private static double getCpuRateForLinux() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader brStat = null;
        StringTokenizer tokenStat;
        try {
            System.out.println("Getting usage rate of CPU , linux version: " + linuxVersion);
            Process process = Runtime.getRuntime().exec("top -b -n 1");
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            brStat = new BufferedReader(isr);
            if (linuxVersion == null || linuxVersion.equals("2.4")) {
                brStat.readLine();
                brStat.readLine();
                brStat.readLine();
                brStat.readLine();
                tokenStat = new StringTokenizer(brStat.readLine());
                tokenStat.nextToken();
                tokenStat.nextToken();
                String user = tokenStat.nextToken();
                tokenStat.nextToken();
                String system = tokenStat.nextToken();
                tokenStat.nextToken();
                String nice = tokenStat.nextToken();
                System.out.println(user + " , " + system + " , " + nice);
                user = user.substring(0, user.indexOf("%"));
                system = system.substring(0, system.indexOf("%"));
                nice = nice.substring(0, nice.indexOf("%"));
                float userUsage = new Float(user);
                float systemUsage = new Float(system);
                float niceUsage = new Float(nice);
                return (userUsage + systemUsage + niceUsage) / 100;
            } else {
                brStat.readLine();
                brStat.readLine();
                tokenStat = new StringTokenizer(brStat.readLine());
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                String cpuUsage = tokenStat.nextToken();
                System.out.println("CPU idle : " + cpuUsage);
                Float usage = new Float(cpuUsage.substring(0, cpuUsage.indexOf("%")));
                return (1 - usage / 100);
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            freeResource(is, isr, brStat);
            return 1;
        } finally {
            freeResource(is, isr, brStat);
        }
    }

    private static void freeResource(InputStream is, InputStreamReader isr,
            BufferedReader br) {
        try {
            if (is != null)
                is.close();
            if (isr != null)
                isr.close();
            if (br != null)
                br.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    /**
     * 获得CPU使用率.
     *
     * @return 返回cpu使用率
     * @author GuoHuang
     */
    private double getCpuRatioForWindows() {
        try {
            String procCmd = System.getenv("windir") + "\\system32\\wbem\\wmic.exe process get Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
            // 取进程信息
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
            Thread.sleep(CPUTIME);
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                long idletime = c1[0] - c0[0];
                long busytime = c1[1] - c0[1];
                return (double) PERCENT * (busytime) / (busytime + idletime);
            } else
                return 0.0;
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 读取CPU信息.
     *
     * @param proc
     * @return
     * @author GuoHuang
     */
    private long[] readCpu(final Process proc) {
        long[] retn = new long[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH)
                return null;
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int kmtidx = line.indexOf("KernelModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx)
                    continue;
                // 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
                // ThreadCount,UserModeTime,WriteOperation
                String caption = StrUtils.substring(line, capidx, cmdidx - 1).trim();
                String cmd = StrUtils.substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.contains("wmic.exe"))
                    continue;
                String s1 = StrUtils.substring(line, kmtidx, rocidx - 1).trim();
                String s2 = StrUtils.substring(line, umtidx, wocidx - 1).trim();
                if (caption.equals("System Idle Process") || caption.equals("System")) {
                    if (s1.length() > 0)
                        idletime += Long.parseLong(s1);
                    if (s2.length() > 0)
                        idletime += Long.parseLong(s2);
                    continue;
                }
                if (s1.length() > 0)
                    kneltime += Long.parseLong(s1);
                if (s2.length() > 0)
                    usertime += Long.parseLong(s2);
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 测试方法.
     *
     * @param args
     * @throws Exception
     * @author GuoHuang
     */
    public static void main(String[] args) throws Exception {
        IMonitorService service = new MonitorServiceImpl();
        MonitorInfoBean monitorInfo = service.getMonitorInfoBean();
        System.out.println("cpu占有率=" + monitorInfo.getCpuRatio());
        System.out.println("可使用内存=" + monitorInfo.getTotalMemory());
        System.out.println("剩余内存=" + monitorInfo.getFreeMemory());
        System.out.println("最大可使用内存=" + monitorInfo.getMaxMemory());
        System.out.println("操作系统=" + monitorInfo.getOsName());
        System.out.println("总的物理内存=" + monitorInfo.getTotalMemorySize() + "kb");
        System.out.println("剩余的物理内存=" + monitorInfo.getFreeMemory() + "kb");
        System.out.println("已使用的物理内存=" + monitorInfo.getUsedMemory() + "kb");
        System.out.println("线程总数=" + monitorInfo.getTotalThread() + "kb");
    }
}

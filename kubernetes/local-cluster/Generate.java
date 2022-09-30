/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package kubernetes.localCluster;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;

public class Generate {
    static final int mb = (int) Math.pow(2, 20);
    private static final double K8S_CPU_OVERHEAD = 1.1;
    private static final long K8S_MEM_OVERHEAD = 1024;

    public static void main(String[] args) throws Exception {
        var df = new DecimalFormat("#,###");

        // check for version arg
        if (args.length < 1) {
            print("Expected at least the version argument");
            printUsage();
            return;
        }
        if (Arrays.asList("-h", "help", "--help").contains(args[0])) {
            printUsage();
            return;
        }

        var version = args[0];
        var cwd = System.getProperty("user.dir");
        print("Script running from %s", cwd);

        // compute viable CPU and memory values
        var processors = Runtime.getRuntime().availableProcessors() / 2;
        if (args.length < 2) {
            print("Assuming that half processors are allocatable in kubernetes: allocatable=%d available=%d", processors, processors * 2);
        } else {
            processors = Integer.parseInt(args[1]);
            print("Allocating specified processors available to kubernetes: allocatable=%d", processors);
        }

        var cellSupervisorCpu = 0.5;
        var sampleSupervisorCpu = 0.5;
        var workerCpu = (double) (processors - 1) - K8S_CPU_OVERHEAD;
        var cellWorkerCpu = workerCpu * 2 / 3.0;
        var sampleWorkerCpu = workerCpu / 3;
        print("Computed processor allocation: cell-supervisor=%s cell-worker=%s samplesource-supervisor=%s samplesource-worker=%s",
              toMilliCpu(cellSupervisorCpu), toMilliCpu(cellWorkerCpu), toMilliCpu(sampleSupervisorCpu), toMilliCpu(sampleWorkerCpu));

        OperatingSystemMXBean systemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        var available = systemMXBean.getTotalPhysicalMemorySize() / mb;
        var allocatableMem = available / 2;

        if (args.length < 3) {
            print("Assuming that half of system memory allocatable by kubernetes: allocatable=%sMi available=%sMi", df.format(allocatableMem), df.format(available));

        } else {
            allocatableMem = Integer.parseInt(args[2]);
            print("Allocating specified memory to kubernetes: allocatable=%d", allocatableMem);
        }

        allocatableMem -= K8S_MEM_OVERHEAD; // pad the available memory
        var sampleSupervisorMb = Math.min(512, allocatableMem / 8);
        var cellSupervisorMb = Math.min(512, allocatableMem / 4);
        var cellWorkerMb = (long) (allocatableMem - sampleSupervisorMb - cellSupervisorMb) / 2;
        var sampleWorkerMb = allocatableMem - sampleSupervisorMb - cellSupervisorMb - cellWorkerMb;

        print("Computed memory allocation: cell-supervisor=%sMi cell-worker=%sMi samplesource-supervisor=%sMi samplesource-worker=%sMi",
              df.format(cellSupervisorMb), df.format(cellWorkerMb), df.format(sampleSupervisorMb), df.format(sampleWorkerMb));

        // load the template
        var k8sBase = Paths.get(cwd, "kubernetes", "local-cluster");
        var template = Files.readString(k8sBase.resolve("Burst.tmpl.yaml"));
        var evaluated = template
                .replaceAll("IMAGE_VERSION", version)
                .replaceAll("CERT_HOST_PATH", k8sBase.resolve("certs").toString())
                .replaceAll("DB_INIT_HOST_PATH", k8sBase.resolve("mysql").resolve("init").toString())
                .replaceAll("DB_DATA_HOST_PATH", k8sBase.resolve("mysql").resolve("data").toString())
                .replaceAll("CELL_SUPER_CPU", toMilliCpu(cellSupervisorCpu))
                .replaceAll("CELL_SUPER_MEM", toMbMem(cellSupervisorMb))
                .replaceAll("SAMPLE_SUPER_CPU", toMilliCpu(sampleSupervisorCpu))
                .replaceAll("SAMPLE_SUPER_MEM", toMbMem(sampleSupervisorMb))
                .replaceAll("CELL_WORKER_CPU", toMilliCpu(cellWorkerCpu))
                .replaceAll("CELL_WORKER_MEM", toMbMem(cellWorkerMb))
                .replaceAll("SAMPLE_WORKER_CPU", toMilliCpu(sampleWorkerCpu))
                .replaceAll("SAMPLE_WORKER_MEM", toMbMem(sampleWorkerMb))
                .replaceAll(" # TODO replace with script", "")
                ;
        // write template out in new file
        Path generatedDir = k8sBase.resolve("generated");
        Path outFile = generatedDir.resolve("Burst.yaml");
        Files.createDirectories(generatedDir);
        Files.writeString(outFile, evaluated);

        print("Start the local cluster by executing:");
        print("kubectl apply -f %s", Paths.get(cwd).relativize(outFile).toString());
    }

    private static void print(String output, Object... args) {
        System.out.printf(output + "%n", args);
    }

    private static void printUsage() {
        print("usage:");
        print("java kubernetes/Generate version [maxCpu] [maxMemory]");
        print("");
        printArg("version", "the docker tag to be replaced used in the kubernetes file");
        printArg("maxCpu", "the maxumim number of CPUs allocatable by kubernetes");
        printArg("maxMemory", "the maximum amount of memory allocatable by kubernetes (in mb)");
    }

    private static void printArg(String argName, String description) {
        print("%s\t\t%s", argName, description);
    }

    public static String toMilliCpu(double milliCpu) {
        return String.format("%.0fm", Math.floor(milliCpu * 1000));
    }

    public static String toMbMem(long memMb) {
        return String.format("%dMi", memMb);
    }
}

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LogParser {

    private static <K, V extends Comparable<V>> LinkedHashMap<K, V> getHighestAvg(Map<K, V> map, int topN) {
        LinkedHashMap<K, V> longestDurations = new LinkedHashMap<>();
        for (int i = 0; i < topN ; i++) {
            Map.Entry<K, V> maxEntry = null;
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                    maxEntry = entry;
                }
            }
            longestDurations.put(maxEntry.getKey(), maxEntry.getValue());
            map.remove(maxEntry.getKey());
        }
        return longestDurations;
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-h")) {
            System.out.println("Run the program as 'java -jar assignment.jar file_name N'");
            System.out.println("1) Pass in the file name as the first argument");
            System.out.println("2) Second argument (integer) is used to filter out top N resources with highest average request durations");
            System.out.println("Make sure the log file is in the correct folder 'build/libs'");
            System.out.println("Example: java -jar assignment.jar timing.log 10");
        } else {
            if (args.length == 2) {
                try {
                    long startTime = System.nanoTime();
                    String fileName = args[0];
                    int topN = Integer.parseInt(args[1]);
                    File file = new File(fileName);

                    if (file.exists() && file.isFile()) {
                        FileInputStream fileInputStream = new FileInputStream(fileName);
                        BufferedReader fileBufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                        String logLine;
                        Map<String, List<Integer>> resources = new HashMap<>();
                        Map<String, Integer> timestamps = new TreeMap<>();

                        while ((logLine = fileBufferedReader.readLine()) != null) {
                            String[] line = logLine.split(" ");
                            String resource = line[4].split("[?.]")[0];
                            resource = resource.startsWith("/") ? resource.substring(1) : resource;
                            String timestamp = line[1].split(":")[0];
                            int logLineLength = line.length;
                            int requestDuration = Integer.parseInt(line[logLineLength - 1]);
                            List<Integer> requestDurations;
                            int hourFrequency;

                            if (resources.containsKey(resource)) {
                                requestDurations = resources.get(resource);
                                requestDurations.add(requestDuration);
                            } else {
                                requestDurations = new ArrayList<>();
                                requestDurations.add(requestDuration);
                            }

                            if (timestamps.containsKey(timestamp)) {
                                hourFrequency = timestamps.get(timestamp);
                                hourFrequency++;
                            } else {
                                hourFrequency = 1;
                            }
                            resources.put(resource, requestDurations);
                            timestamps.put(timestamp, hourFrequency);
                        }
                        fileInputStream.close();

                        Map<String, Integer> averageRequestDurationPerResource = new HashMap<>();
                        for (String key : resources.keySet()) {
                            List<Integer> resourceRequestDurations = resources.get(key);
                            int sumResourceRequestDurations = resourceRequestDurations.stream().mapToInt(Integer::intValue).sum();
                            int averageDuration = sumResourceRequestDurations / resourceRequestDurations.size();
                            averageRequestDurationPerResource.put(key, averageDuration);
                        }

                        Map<String, Integer> topRequests;
                        if (topN > averageRequestDurationPerResource.size()) {
                            topRequests = averageRequestDurationPerResource;
                        } else {
                            topRequests = getHighestAvg(averageRequestDurationPerResource, topN);
                        }

                        for (Map.Entry request : topRequests.entrySet()) {
                            System.out.println("Resource: "+ request.getKey() + " | Average request duration: " + request.getValue() + " ms");
                        }

                        System.out.print("\n  Hour     Frequency");
                        for (String key : timestamps.keySet()) {
                            System.out.printf("\n%5s%13d", key, timestamps.get(key));
                        }

                        long durationInNano = System.nanoTime() - startTime;
                        long durationInMillis = TimeUnit.NANOSECONDS.toMillis(durationInNano);
                        System.out.println("\nTime taken to run the program: " + durationInMillis + " ms.");
                    } else {
                        System.err.println("ERROR: Couldn't read designated file.");
                    }
                } catch (IOException e) {
                    System.err.println("ERROR: Couldn't read designated file.");
                    System.err.println("Try 'java -jar assignment.jar -h' for more information.");
                    System.exit(1);
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: Second argument must be an integer.");
                    System.err.println("Try 'java -jar assignment.jar -h' for more information.");
                    System.exit(1);
                }
            } else {
                System.err.println("Try 'java -jar assignment.jar -h' for more information.");
            }
        }
    }
}
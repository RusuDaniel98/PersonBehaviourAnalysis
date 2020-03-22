import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MonitoredData {
    private String startTime, endTime, activity;
    static Map<String, Long> activityHasValue = new HashMap<>();

    public MonitoredData(String startTime, String endTime, String activity){
        this.startTime = startTime;
        this.endTime = endTime;
        this.activity = activity;
    }

    //method that reads the content in the file, builds mane MonitoredData objects and adds the to a list.
    public static ArrayList<MonitoredData> readFileData(){
        ArrayList<MonitoredData> data = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get("Activities.txt"))) {
                lines = stream
                        .flatMap((line->Stream.of(line.split("\t\t"))))
                        .collect(Collectors.toList());
                for(int i=0; i<lines.size()-2; i+=3){
                    MonitoredData md = new MonitoredData(lines.get(i), lines.get(i+1), lines.get(i+2));
                    data.add(md);
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //data.forEach(System.out::println);
        return data;
    }

    //method that returns the number of distinct days
    public static int countDays(List<MonitoredData> data){
        System.out.println("3) How many times has appeared each activity for each day: ");
        long result = 0;
        ArrayList<String> days = new ArrayList<>();
        ArrayList<MonitoredData> dataTrunc = new ArrayList<>();
        for (int i=0; i<data.size(); i++){
            String day = "";
            day += data.get(i).getStartTime().charAt(8);
            day += data.get(i).getStartTime().charAt(9);
            days.add(day);
        }
        for (int i=0; i<data.size(); i++){
            dataTrunc.add(data.get(i));
            if (i<days.size()-1){
                if (!days.get(i+1).equals(days.get(i))){
                    System.out.println("   Day " + days.get(i) + " ::: " + countActivitiesWholePeriod(dataTrunc));
                    dataTrunc.clear();
                }
            }else{
                System.out.println("   Day " + days.get(i) + " ::: " + countActivitiesWholePeriod(dataTrunc));
            }
        }
        Stream<String> daysStream = days.stream();
        result = daysStream
                .distinct()
                .count();
        return (int)result;
    }

    //method that counts how many times has each activity appeared over the entire period
    public static Map<String, Long> countActivitiesWholePeriod(List<MonitoredData> data){
        ArrayList<String> activities = new ArrayList<>();
        for(int i=0; i<data.size(); i++){
            activities.add(data.get(i).getActivity());
        }
        Map<String, Long> counts =
                activities.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        return counts;
    }

    // 3) count how many times has appeared each activity for each day
    //    DONE in the countDays method above. :)

    // 4) For each line from the file map for the activity label the duration recorded on that line (end time - start time)
    public static Map<String, Long> eachLineDuration(List<MonitoredData> data){
        Map<String, String> hash = new HashMap<>();
        Map<String, Long> hashWithMillis = new HashMap<>();
        for (int i=0; i<data.size(); i++){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                String startTime = data.get(i).getStartTime();
                Date firstDate = sdf.parse(startTime);
                String endTime = data.get(i).getEndTime();
                Date secondDate = sdf.parse(endTime);
                long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
                DateFormat df = new SimpleDateFormat("HH 'hours', mm 'mins,' ss 'seconds'");
                df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                String difference = df.format(new Date(diffInMillies));
                hash.put(data.get(i).getActivity(), difference);
                System.out.println("  " + data.get(i).getActivity() + " ::: " + hash.get(data.get(i).getActivity()));
                //-----For each activity compute the entire duration over the monitoring period
                String acti = data.get(i).getActivity();
                if (activityHasValue.containsKey(data.get(i).getActivity())){
                    long index = hashWithMillis.get(acti);
                    hashWithMillis.put(data.get(i).getActivity(), diffInMillies + index);
                }else{
                    hashWithMillis.put(data.get(i).getActivity(), diffInMillies);
                    activityHasValue.put(data.get(i).getActivity(), diffInMillies);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        System.out.print("\n\n5) Each activity duration over the whole period: \n");
        System.out.println(hashWithMillis);
        return hashWithMillis;
    }

    // 5) For each activity compute the entire duration over the monitoring period
    //    Done in eachLineDuration method above. :)

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getActivity() {
        return activity;
    }

    public static void main(String[] args){
        List<MonitoredData> data = readFileData();
        System.out.print("\n1) Total number of days: " + countDays(data) + "\n\n");
        System.out.println("2) Activities count over the whole period: ");
        System.out.print("   " + countActivitiesWholePeriod(data) + "\n\n");
        System.out.println("4) Each line duration: ");
        eachLineDuration(data);


    }


}

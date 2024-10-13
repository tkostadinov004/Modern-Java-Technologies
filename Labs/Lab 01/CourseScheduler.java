import java.util.Arrays;
import java.util.Comparator;

public class CourseScheduler {
    public static int maxNonOverlappingCourses(int[][] courses) {
        Arrays.sort(courses, new Comparator<int[]>() {
            public int compare(int[] course1, int[] course2) {
                return course1[1] - course2[1];
            }
        });

        int count = 0;
        int maxEnd = -1;
        for (int[] course : courses) {
            if (course[0] >= maxEnd) {
                maxEnd = course[1];
                count++;
            }
        }
        return count;
    }
}
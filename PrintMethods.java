import com.github.tomakehurst.wiremock.extension.Parameters;

public class PrintMethods {
    public static void main(String[] args) throws Exception {
        Parameters p = new Parameters();
        try {
            System.out.println(p.getString("prefix"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

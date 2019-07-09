package neurones.java.fr;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.ArrayList;

/**
 * Constructor
 */
public class Excel {

    private static int count = 0;
    private static HSSFWorkbook workbook = new HSSFWorkbook();
    private static HSSFSheet sheet = workbook.createSheet("FirstSheet");

    /**
     * Getter Workbook
     * @return The workbook of the instance
     */
    public HSSFWorkbook getWorkbook() {
        return workbook;
    }

    /**
     * Generate a workbook with tops
     *
     * @param arr      Array of vms
     * @param sent     Sentence to write in cell 3
     * @param resource Which resource is in the top
     */
    public void generate(ArrayList<VM> arr, String sent, String resource) {
        int i;
        try {

            HSSFRow rowHead = sheet.createRow((short) count);
            rowHead.createCell(0).setCellValue(sent);
            ++count;
            HSSFRow rowTwo = sheet.createRow((short) count);
            rowTwo.createCell(0).setCellValue("N°");
            rowTwo.createCell(1).setCellValue("Name");
            rowTwo.createCell(2).setCellValue(sent);
            rowTwo.createCell(3).setCellValue("Recommendation");
            ++count;

            for (i = 0; i < arr.size(); ++i) {
                VM vm = arr.get(i);
                HSSFRow row = sheet.createRow((short) count);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(vm.getName());
                switch (resource) {
                    case "cpu":
                        row.createCell(2).setCellValue(vm.getCpuUsage());
                        if (vm.getCpuRecomm() == -1) {
                            row.createCell(3).setCellValue("/");
                        } else {
                            row.createCell(3).setCellValue(vm.getCpuRecomm());
                        }
                        break;
                    case "ram":
                        row.createCell(2).setCellValue(vm.getRamUsage() / 1000000);
                        if (vm.getRamRecomm() == -1) {
                            row.createCell(3).setCellValue("/");
                        } else {
                            row.createCell(3).setCellValue(vm.getRamRecomm());
                        }
                        break;
                    case "iops":
                        row.createCell(2).setCellValue(vm.getIOPS());
                        row.createCell(3).setCellValue("/");
                        break;
                    case "diskspace":
                        row.createCell(2).setCellValue(vm.getDiskSpace());
                        if (vm.getDiskRecomm() == -1) {
                            row.createCell(3).setCellValue("/");
                        } else {
                            row.createCell(3).setCellValue(vm.getDiskRecomm());
                        }
                        break;
                    default:
                        break;
                }
                ++count;
            }
            ++count;
        } catch (Exception err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }

    /**
     * Add alerts to the workbook
     *
     * @param alerts Array with all the alerts
     */
    public void addAlerts(ArrayList<Alert> alerts) {
        int i;
        try {
            HSSFRow title = sheet.createRow((short) count);
            title.createCell(0).setCellValue("All the alerts");
            ++count;
            HSSFRow row = sheet.createRow((short) count);
            row.createCell(0).setCellValue("Name");
            row.createCell(1).setCellValue("Type");
            row.createCell(2).setCellValue("Status");
            row.createCell(3).setCellValue("Content");
            ++count;
            for (i = 0; i < alerts.size(); ++i) {
                Alert alert = alerts.get(i);
                HSSFRow roww = sheet.createRow((short) count);
                roww.createCell(0).setCellValue(alert.getName());
                roww.createCell(1).setCellValue(alert.getType());
                roww.createCell(2).setCellValue(alert.getStatus());
                roww.createCell(3).setCellValue(alert.getContent());
                ++count;
            }
            ++count;
        } catch (Exception err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }
}

package BSUTimetableRest.word;

import BSUTimetableRest.entity.Day;
import BSUTimetableRest.entity.GroupTimetable;
import BSUTimetableRest.entity.Lesson;
import BSUTimetableRest.entity.LessonInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelController {

    private List<String> daysOfWeek = new ArrayList<>();
    private int row = 0;
    private final Sheet sheet;
    private Workbook excel;


    public ExcelController(String path) {
        daysOfWeek.add("ПОНЕДЕЛЬНИК");
        daysOfWeek.add("ВТОРНИК");
        daysOfWeek.add("СРЕДА");
        daysOfWeek.add("ЧЕТВЕРГ");
        daysOfWeek.add("ПЯТНИЦА");
        daysOfWeek.add("СУББОТА");


        try {
            excel = new XSSFWorkbook(new FileInputStream(new File(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        sheet = excel.getSheetAt(0);
    }


    public List<GroupTimetable> getGroupTimetable() {
        List<GroupTimetable> groupTimetables = new ArrayList<>();

        //Find start of table
        List<Cell> cellsStart = new ArrayList<>();
        while(cellsStart.size()<2) {
            cellsStart = parseRow(sheet);
            row++;
        }
        row--;

        //Getting groups info
        List<List<Cell>> listOfCells = new ArrayList<>();
        //Parse rows until find bottom border
        while(sheet.getRow(row).getCell(0).getCellStyle().getBorderBottom().getCode()==0) {
            listOfCells.add(parseRow(sheet));
            row++;
        }
        listOfCells.add(parseRow(sheet));

        List<List<String>> listOfGroupsInfo = new ArrayList<>();

        for(int i = 2; i < listOfCells.get(0).size(); i++) {
            List<String> lessonNames = new ArrayList<>();
            for(int j = 0; j < listOfCells.size(); j++) {
                if(!listOfCells.get(j).get(i).toString().isEmpty())
                    lessonNames.add( listOfCells.get(j).get(i).toString());
            }

            listOfGroupsInfo.add(new ArrayList<>(lessonNames));
        }
        //listOfGroupsInfo.forEach(System.out::println);

        row=0;
        List<Day> dayParsed = parseDay(sheet);

        //dayParsed.forEach(System.out::println);

        for(int i = 0; i < listOfGroupsInfo.size(); i++) {
            List<String> groupInfo = listOfGroupsInfo.get(i);
            String groupName = groupInfo.get(0).replaceAll("\\s","");
            groupInfo.remove(0);

            List<Day> days = new ArrayList<>();
            //System.out.println(groupName);
            for(int j = i; j < dayParsed.size(); j+=4) {
                days.add(dayParsed.get(j));
                //System.out.println(j + "\t\t" + dayParsed.get(j).toString());
            }

            groupTimetables.add(GroupTimetable.builder()
                    .groupName(groupName)
                    .days(days)
                    .build()
            );
        }

        try {
            excel.close();
        }catch (Exception e) {e.printStackTrace();}
        return groupTimetables;

    }

    private List<Day> parseDay(Sheet sheet){
        List<Day> days = new ArrayList<>();

        int currentDayOfWeek = 0;


        //Find start of timetable
        while(!parseRow(sheet).get(0).toString().equalsIgnoreCase(daysOfWeek.get(currentDayOfWeek))) {
            row++;
        }
        currentDayOfWeek++;
        row++;



        while (currentDayOfWeek < daysOfWeek.size()) {
            List<List<Lesson>> listOfLessons = new ArrayList<>();


            //Get lessons for current day until get next day
            while (!parseRow(sheet).get(0).toString().equalsIgnoreCase(daysOfWeek.get(currentDayOfWeek))) {


                try{
                    if(parseRow(sheet,row).size()>2)
                        listOfLessons.add(parseLesson(sheet));
                } catch (Exception e) {e.printStackTrace(); }
                row++;
            }
            //System.out.println((row+1) + " | " + parseRow(sheet).get(0));


            for (int row = 0; row < listOfLessons.get(0).size(); row++) {
                List<Lesson> lessons = new ArrayList<>();
                for (int column = 0; column < listOfLessons.size(); column++) {
                    lessons.add(listOfLessons.get(column).get(row));
                }
                Day day = Day.builder()
                        .dayName(daysOfWeek.get(currentDayOfWeek - 1))
                        .lessons(lessons)
                        .build();
                days.add(day);
            }
            currentDayOfWeek++;
            row++;
        }

        List<List<Lesson>> listOfLessons = new ArrayList<>();
        int lastRowNum = sheet.getLastRowNum();
        while (row < lastRowNum-1) {
            try{listOfLessons.add(parseLesson(sheet)); } catch (Exception e) {}
            row++;
        }


        //Creating days
        for (int row = 0; row < listOfLessons.get(0).size(); row++) {
            List<Lesson> lessons = new ArrayList<>();
            for (int column = 0; column < listOfLessons.size(); column++) {
                lessons.add(listOfLessons.get(column).get(row));
            }
            Day day = Day.builder()
                    .dayName(daysOfWeek.get(currentDayOfWeek - 1))
                    .lessons(lessons)
                    .build();
            days.add(day);
        }
        row++;

        return days;
    }

    private List<Lesson> parseLesson(Sheet sheet) {
        List<Lesson> lessons = new ArrayList<>();

        List<List<Cell>> listOfCells = new ArrayList<>();
        //Parse rows until find bottom border
        while(sheet.getRow(row).getCell(0).getCellStyle().getBorderBottom().getCode()==0) {
            listOfCells.add(parseRow(sheet));
            row++;
        }
        listOfCells.add(parseRow(sheet));

        //Getting time, lesson number
        String lessonTime = listOfCells.get(0).get(0).toString();
        try{
            lessonTime = (listOfCells.get(0).get(0) + " " + listOfCells.get(1).get(0)).replaceAll("\\s","");
        } catch (Exception ignored) {}
        String lessonNumber = listOfCells.get(0).get(1).toString().replace(".0","");


        //Get lessonNames and create lessons
        for(int i = 2; i < listOfCells.get(0).size(); i++) {
            List<String> lessonNames = new ArrayList<>();
            String lessonName = "";
            for(int j = 0; j < listOfCells.size(); j++) {
                if(!listOfCells.get(j).get(i).toString().isEmpty()) {
                    lessonName += listOfCells.get(j).get(i).toString();
                }
                //System.out.println("-->"+lessonName);
                if(listOfCells.get(j).get(i).getCellStyle().getBorderBottom().getCode()!=0) {

                    if(!lessonName.isEmpty()) {
                        lessonNames.add(lessonName);
                        lessonName = "";
                    }

                }
            }
            List<LessonInfo> lessonInfos = new ArrayList<>();

            for(String lesName: lessonNames) {
                String[] lesInfo = lesName.split(";");
                if(lesInfo.length==4) {
                    LessonInfo lessonInfo = LessonInfo.builder()
                            .lessonName(lesInfo[0])
                            .lessonType(lesInfo[1])
                            .lessonPeriod(lesInfo[2])
                            .lessonLecturer("")
                            .lessonPlace(lesInfo[3])
                            .build();

                    lessonInfos.add(lessonInfo);
                }
                if(lesInfo.length==5) {
                    LessonInfo lessonInfo = LessonInfo.builder()
                            .lessonName(lesInfo[0])
                            .lessonType(lesInfo[1])
                            .lessonPeriod(lesInfo[2])
                            .lessonLecturer(lesInfo[3])
                            .lessonPlace(lesInfo[4])
                            .build();

                    lessonInfos.add(lessonInfo);
                };
            }

            Lesson lesson = Lesson.builder()
                    .lessonTime(lessonTime)
                    .lessonNumber(lessonNumber)
                    .lessonInfo(lessonInfos)
                    .build();

            lessons.add(lesson);
        }


        return lessons;
    }


    private List<Cell> parseRow(Sheet sheet) {
        List<Cell> cells = new ArrayList<>();
        Iterator<Cell> cellIterator = sheet.getRow(row).cellIterator();

        boolean repeat = false;
        Cell cellRepeat = null;
        while(cellIterator.hasNext()) {
            Cell c = cellIterator.next();
            //System.out.println("-->" + c + " | " + c.getCellStyle().getBorderRight().getCode());
            if(!repeat) {
                cells.add(c);
            } else {
                cells.add(cellRepeat);
                repeat = false;
            }
            if(c.getCellStyle().getBorderRight().getCode()==0) {
                repeat = true;
                cellRepeat = cells.get(cells.size()-1);
            }
        }

        return cells;
    }

    private List<Cell> parseRow(Sheet sheet, int row) {
        List<Cell> cells = new ArrayList<>();
        Iterator<Cell> cellIterator = sheet.getRow(row).cellIterator();

        boolean repeat = false;
        Cell cellRepeat = null;
        while(cellIterator.hasNext()) {
            Cell c = cellIterator.next();
            //System.out.println("-->" + c + " | " + c.getCellStyle().getBorderRight().getCode());
            if(!repeat) {
                cells.add(c);
            } else {
                cells.add(cellRepeat);
                repeat = false;
            }
            if(c.getCellStyle().getBorderRight().getCode()==0) {
                repeat = true;
                cellRepeat = cells.get(cells.size()-1);
            }
        }

        return cells;
    }

}

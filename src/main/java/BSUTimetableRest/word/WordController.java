package BSUTimetableRest.word;

import BSUTimetableRest.entity.Day;
import BSUTimetableRest.entity.GroupTimetable;
import BSUTimetableRest.entity.Lesson;
import BSUTimetableRest.entity.LessonInfo;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordController {

    private List<String> daysOfWeek = new ArrayList<>();

    //Номер столбца и ширина строки столбца
    private Map<Integer, Integer> widthByNum = new HashMap<>();
    //Название столбцов с названиями групп
    private List<String> headerNames = new ArrayList<>();
    //Номер столбика , где Часы пишутся второй раз в headerNames
    private int secondHoursNum = 0;
    private int currentRow = 0;

    public List<GroupTimetable> parseWord(File file) {
        fillDaysOfWeek();

        try {
            XWPFDocument document = new XWPFDocument(new FileInputStream(file));
            List<XWPFTable> tables = document.getTables();

            List<GroupTimetable> groupTimetables = new ArrayList<>();

            for(XWPFTable table : tables) {
                groupTimetables.addAll(parseTable(table));
            }

            return groupTimetables;
        } catch (Exception e) {e.printStackTrace();}

        return null;
    }

    private List<GroupTimetable> parseTable(XWPFTable table) {
        fillDaysOfWeek();
        widthByNum = new HashMap<>();
        headerNames = new ArrayList<>();
        secondHoursNum = 0;
        currentRow = 0;
        return parseGroupTimetables(table);
    }


    private List<GroupTimetable> parseGroupTimetables(XWPFTable table) {
        fillWidthByNumAndSecondHoursNum(table);
        List<GroupTimetable> groupTimetables = new ArrayList<>();

        List<Day> days = parseDays(table);
        headerNames.remove(0);
        headerNames.remove(0);
        headerNames.remove("Часы;");
        for(int i = 0; i < headerNames.size(); i++) {
            List<Day> daysForGroupTimetables = new ArrayList<>();
            for(int j = i; j < days.size(); j+=headerNames.size()) {
                daysForGroupTimetables.add(days.get(j));
            }
            GroupTimetable groupTimetable = GroupTimetable.builder()
                    .groupName(headerNames.get(i).replace(";",""))
                    .days(daysForGroupTimetables)
                    .build();
            groupTimetables.add(groupTimetable);
        }

        return groupTimetables;
    }

    private List<Day> parseDays(XWPFTable table) {
        List<Day> days = new ArrayList<>();

        //Поиск понедельника
        String firstTextFromRow = "";
        try{
            firstTextFromRow = parseRow(table, currentRow).get(0);
        } catch (Exception e) {}
        while(daysOfWeek.stream().noneMatch(firstTextFromRow::equalsIgnoreCase)) {
            currentRow++;
            try {
                firstTextFromRow = parseRow(table, currentRow).get(0);
            }catch (Exception e) {}
        }
        currentRow++;

        //Добавляем в массив days Day
        while(currentRow < table.getRows().size()) {
            List<Lesson> lessons = new ArrayList<>();
            String currentDay = firstTextFromRow;
            firstTextFromRow = "";
            currentDay = formatDayName(currentDay);
            while (daysOfWeek.stream().noneMatch(firstTextFromRow::equalsIgnoreCase)) {
                try {
                    lessons.addAll(parseLesson(table, currentRow));
                } catch (Exception ignored) {break;}
                List<String> parsedRow = parseRow(table, currentRow);
                if(parseRow(table, currentRow)!=null)
                    firstTextFromRow = parsedRow.get(0);
            }


            // headerNames.size() - 3) - количество групп, 3 - это Часы, номер (пустая строка в docx), Часы
            for (int i = 0; i < (headerNames.size() - 3); i++) {
                List<Lesson> lessonForDays = new ArrayList<>();
                for (int j = i; j < lessons.size(); j += (headerNames.size() - 3)) {
                    lessonForDays.add(lessons.get(j));
                }
                //if(currentDay.equalsIgnoreCase("Суббота"))
                //    lessonForDays.forEach(System.out::println);
                Day day = Day.builder()
                        .dayName(currentDay)
                        .lessons(lessonForDays)
                        .build();

                days.add(day);
            }
            currentRow++;

        }

        return days;
    }

    private List<Lesson> parseLesson(XWPFTable table, int row) {
        List<Lesson> lessons = new ArrayList<>();

        //Получаем набор строк урока до начала нового урока
        List<List<String>> listOfRowsInfo = new ArrayList<>();

        List<String> addRowInfo = parseRow(table, row);
        listOfRowsInfo.add(addRowInfo);
        row++;
        addRowInfo = parseRow(table,row);
        while(addRowInfo !=null && addRowInfo.get(0).isEmpty()){
            listOfRowsInfo.add(addRowInfo);
            row++;
            currentRow++;
            addRowInfo = parseRow(table,row);
        }

        String timeFirst = listOfRowsInfo.get(0).get(0);
        String timeSecond = listOfRowsInfo.get(0).get(secondHoursNum);
        String lessonNum = listOfRowsInfo.get(0).get(1);

        //Добавление уроков(пар) с начала  до столбца со вторым временем
        for(int i = 2; i < secondHoursNum; i++) {
            List<String> lessonName = new ArrayList<>();
            for(int j = 0; j < listOfRowsInfo.size(); j++) {
                String lessonNameTemp = listOfRowsInfo.get(j).get(i);
                if (!lessonNameTemp.isEmpty()) {
                    lessonName.add(lessonNameTemp);
                }
            }

            List<LessonInfo> lessonInfos = new ArrayList<>();

            for(String lesName: lessonName) {
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
                    .lessonTime(timeFirst)
                    .lessonNumber(lessonNum)
                    .lessonInfo(lessonInfos)
                    .build();

            lessons.add(lesson);
        }

        //Добавление уроков(пар) со столбца со вторым временем до конца
        for(int i = secondHoursNum+1; i < listOfRowsInfo.get(0).size(); i++) {
            List<String> lessonName = new ArrayList<>();
            for(int j = 0; j < listOfRowsInfo.size(); j++) {
                String lessonNameTemp = listOfRowsInfo.get(j).get(i);
                if(!lessonNameTemp.isEmpty()) {
                    lessonName.add(lessonNameTemp);
                }
            }

            List<LessonInfo> lessonInfos = new ArrayList<>();

            for(String lesName: lessonName) {
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
                    .lessonTime(timeFirst)
                    .lessonNumber(lessonNum)
                    .lessonInfo(lessonInfos)
                    .build();

            lessons.add(lesson);
        }

        currentRow++;
        return lessons;
    }

    private List<String> parseRow(XWPFTable table, int row) {
        List<String> rowText = new ArrayList<>();

        int cellNum = 0;
        if(table.getRow(row)!=null) {
            for (int i = 0; i < table.getRow(row).getTableCells().size(); i++) {
                XWPFTableCell cell = table.getRow(row).getTableCells().get(i);
                int cellWidth = cell.getWidth();

                if (widthByNum.get(cellNum) != null) {
                    if (cellWidth > widthByNum.get(cellNum)) {
                        while (cellWidth > 1000) {
                            rowText.add(cell.getText());
                            cellWidth -= widthByNum.get(i);
                            cellNum++;
                        }
                    } else {
                        rowText.add(cell.getText());
                        cellNum++;
                    }
                } else {
                    rowText.add(cell.getText());
                }
            }
        } else {
            rowText=null;
        }


        return rowText;
    }

    private String formatDayName(String dayName) {
        dayName = dayName.toLowerCase();
        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
    }

    private void fillWidthByNumAndSecondHoursNum(XWPFTable table) {
        //Ищем строчку, где пишутся названия групп
        int rowNum = 0;
        XWPFTableRow row = table.getRow(rowNum);

        for(XWPFTableCell cell : row.getTableCells()) {
            if (cell.getText().contains("профиль")) {
                rowNum++;
                break;
            }
        }

        row = table.getRow(rowNum);

        //Заполнение widthByNum
        for(int i = 0; i < row.getTableCells().size(); i ++) {
            XWPFTableCell cell = row.getTableCells().get(i);
            widthByNum.put(i, cell.getWidth());
            headerNames.add(cell.getText());
        }

        //Заполнение secondHoursNum
        int count = 0;
        for(int i = 0; i < row.getTableCells().size(); i++) {
            XWPFTableCell cell = row.getTableCells().get(i);
            if(cell.getText().equalsIgnoreCase("Часы;")) {
                count++;
                if(count==2) {
                    secondHoursNum = i;
                    break;
                }
            }
        }
    }

    private void fillDaysOfWeek() {
        daysOfWeek.add("понедельник;");
        daysOfWeek.add("вторник;");
        daysOfWeek.add("среда;");
        daysOfWeek.add("четверг;");
        daysOfWeek.add("пятница;");
        daysOfWeek.add("суббота;");
    }

}

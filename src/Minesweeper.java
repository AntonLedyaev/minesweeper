import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class Minesweeper {

    static  Area[][] area;
    static boolean isLoaded = false;

    public static void main(String[] args) {
        Minesweeper.play();
    }

    public static void play(){
        final int numberOfBombs;
        boolean isItWin;
        final Scanner scanner = new Scanner(System.in);
        System.out.println("Поиграем в сапера!");
        area = pickLengthsOfAreaOrLoadGame(scanner);
        if (!Minesweeper.isLoaded) {
            numberOfBombs = getHowManyBombs(area,scanner);
            fillArea(area,numberOfBombs);
        }
        System.out.println("Привет");

        while(true) {
            printArea(area);
            if(playerTurns(area,scanner)) {
                isItWin=false;
                break;
            }
            if(noMoreEmptyAreaAndFlaggedOnlyBombs(area)) {
                isItWin=true;
                break;
            }
        }
        if(isItWin) {
            System.out.println("Победа!");
        }
        else {
            System.out.println("Вы проиграли!");
        }
    }

    private static boolean noMoreEmptyAreaAndFlaggedOnlyBombs(Area[][] area) {
        for (Area[] areas : area) {
            for (int x = 0; x < area[0].length; x++) {
                if (areas[x].getValueOfArea() != ValueOfArea.BOMB/*is not bomb*/ && (areas[x].getStatusOfArea() == StatusOfArea.CLOSED || areas[x].getStatusOfArea() == StatusOfArea.MARKEDASBOMB)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean playerTurns(Area[][] area,Scanner scanner) {
        while(true) {
            System.out.println("Напишите \"x y open \", если вы хотите открыть эту клетку, Напишите \"x y flag\", если вы хотите поставить флаг или убрать его с клетки, напишите \"x y save\", если вы хотите сохранить игру");
            String[] commandAndXAndY = scanner.nextLine().split(" ");
            if(commandAndXAndY.length!=3) {
                System.out.println("напишите запрос корректно!");
            }
            else if(!commandAndXAndY[2].equals("open") && !commandAndXAndY[2].equals("flag") &&!commandAndXAndY[2].equals("save") ) {
                System.out.println("последняя команда должна быть \"open\" or \"flag\"!");
            }
            else if(!isNumeric(commandAndXAndY[0]) || !isNumeric(commandAndXAndY[1])) {
                System.out.println("x и y должны быть числами!");
            }
            else if(!isXandYIn(Integer.parseInt(commandAndXAndY[0]),Integer.parseInt(commandAndXAndY[1]),area)) {
                System.out.println("x и y должны находиться в пределах поля! P.S.: area.lengthY="+area.length+", area.lengthX="+area[0].length);
            }
            else {
                int y = Integer.parseInt(commandAndXAndY[1]),x = Integer.parseInt(commandAndXAndY[0]);
                if(commandAndXAndY[2].equals("open")) {
                    if(area[y][x].getValueOfArea()==ValueOfArea.BOMB) {
                        return true;
                    }
                    else {
                        area[y][x].setStatusOfArea(StatusOfArea.OPENED);
                        if(area[y][x].getValueOfArea()==ValueOfArea.NOONEBOMBAROUND) {
                            openAllAround(x,y,area, new ArrayList<>());
                        }
                        return false;
                    }
                }
                else if(commandAndXAndY[2].equals("save")) {
                    System.out.println("Назовите игру");
                    String saveName = scanner.nextLine();
                    saveName = saveName + ".mine";
                    try {
                        SavedState state = new SavedState(Minesweeper.area);

                        OutputStream file = new FileOutputStream(saveName);
                        OutputStream buffer = new BufferedOutputStream(file);
                        ObjectOutput output = new ObjectOutputStream(buffer);
                        output.writeObject(state);
                        output.flush();
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else { // flag
                    area[y][x].setStatusOfArea(StatusOfArea.MARKEDASBOMB);
                    return false;
                }
            }
        }
    }

    private static void openAllAround(int x, int y, Area[][] area,ArrayList<AreaWithXandY> weOpenedThese) {
        AreaWithXandY[] areasAround = getAllAreasAroundWithTheirXandY(area,x,y);
        for(AreaWithXandY a:areasAround) {
            if(!weOpenedThese.contains(a)) {
                a.getArea().setStatusOfArea(StatusOfArea.OPENED);
                weOpenedThese.add(a);
                if(a.getArea().getValueOfArea()==ValueOfArea.NOONEBOMBAROUND) {
                    openAllAround(a.getX(),a.getY(),area,weOpenedThese);
                }
            }
        }
    }

    private static void printArea(Area[][] area) {
        System.out.println();
        for (Area[] areas : area) {
            for (int x = 0; x < area[0].length; x++) {
                if (areas[x].getStatusOfArea() == StatusOfArea.CLOSED) {
                    System.out.print(".");
                } else if (areas[x].getStatusOfArea() == StatusOfArea.OPENED) {
                    System.out.print(areas[x].getValueOfArea().getIcon());
                } else if (areas[x].getStatusOfArea() == StatusOfArea.MARKEDASBOMB) {
                    System.out.print("*");
                }
            }
            System.out.println();
        }
    }

    private static void fillArea(Area[][] area, int howManyBombs) {
        fillAreaWithBombs(area,howManyBombs);
        fillAreaWithEmptyArea(area);
    }
    private static void fillAreaWithEmptyArea(Area[][] area) {
        for(int y = 0;y<area.length;y++) {
            for(int x=0;x<area[0].length;x++) {
                if(area[y][x] == null) {
                    int howManyBombsAround=0;
                    Area[] areasAround = getAllAreasAround(area,x,y);
                    for(Area a:areasAround) {
                        if(a!=null && a.getValueOfArea()==ValueOfArea.BOMB) {
                            howManyBombsAround++;
                        }
                    }
                    area[y][x] = new Area(ValueOfArea.getValueOfAreaByString(Integer.toString(howManyBombsAround)));
                }
            }
        }
    }
    private static Area[] getAllAreasAround(Area[][] area,int x,int y) {
        Area[] areasAround = new Area[8];
        int i=0;
        if(y!=0 && x!=0) { //left up
            areasAround[i] = area[y-1][x-1];
            i++;
        }
        if(y!=0) { // up
            areasAround[i] = area[y-1][x];
            i++;
        }
        if(x!=area[0].length-1 && y!=0) { //right up
            areasAround[i] = area[y-1][x+1];
            i++;
        }
        if(x!=0) { // left
            areasAround[i] = area[y][x-1];
            i++;
        }
        if(x!=area[0].length-1) { //right
            areasAround[i] = area[y][x+1];
            i++;
        }
        if(x!=0 && y!=area.length-1) { // left down
            areasAround[i] = area[y+1][x-1];
            i++;
        }
        if(y!= area.length-1) { //down
            areasAround[i] = area[y+1][x];
            i++;
        }
        if(x!=area[0].length-1 && y!=area.length-1) { // right down
            areasAround[i] = area[y+1][x+1];
            i++;
        }
        Area[] areasAroundWhithoutNullObjects = new Area[i];
        for(int b = 0;b<i;b++) {
            areasAroundWhithoutNullObjects[b] = areasAround[b];
        }
        return areasAroundWhithoutNullObjects;
    }
    private static AreaWithXandY[] getAllAreasAroundWithTheirXandY(Area[][] area,int x,int y) {
        AreaWithXandY[] areasAroundWithXandY = new AreaWithXandY[8];
        int i=0;
        if(y!=0 && x!=0) { //left up
            areasAroundWithXandY[i] = new AreaWithXandY(x-1,y-1,area[y-1][x-1]);
            i++;
        }
        if(y!=0) { // up
            areasAroundWithXandY[i] = new AreaWithXandY(x,y-1,area[y-1][x]);
            i++;
        }
        if(x!=area[0].length-1 && y!=0) { //right up
            areasAroundWithXandY[i] = new AreaWithXandY(x+1,y-1,area[y-1][x+1]);
            i++;
        }
        if(x!=0) { // left
            areasAroundWithXandY[i] = new AreaWithXandY(x-1,y,area[y][x-1]);
            i++;
        }
        if(x!=area[0].length-1) { //right
            areasAroundWithXandY[i] = new AreaWithXandY(x+1,y,area[y][x+1]);
            i++;
        }
        if(x!=0 && y!=area.length-1) { // left down
            areasAroundWithXandY[i] = new AreaWithXandY(x-1,y+1,area[y+1][x-1]);
            i++;
        }
        if(y!= area.length-1) { //down
            areasAroundWithXandY[i] = new AreaWithXandY(x,y+1,area[y+1][x]);
            i++;
        }
        if(x!=area[0].length-1 && y!=area.length-1) { // right down
            areasAroundWithXandY[i] = new AreaWithXandY(x+1,y+1,area[y+1][x+1]);
            i++;
        }
        AreaWithXandY[] areasAroundWithXandYWhithoutNullObjects = new AreaWithXandY[i];
        for(int b = 0;b<i;b++) {
            areasAroundWithXandYWhithoutNullObjects[b] = areasAroundWithXandY[b];
        }
        return areasAroundWithXandYWhithoutNullObjects;
    }
    private static void fillAreaWithBombs(Area[][] area, int howManyBombs) {
        ArrayList<Integer> listOfAllNumbers = new ArrayList<>(area.length*area[0].length);
        for(int i=0;i<area.length*area[0].length;i++) {
            listOfAllNumbers.add(i,i);
        }
        int tempId,y,x;
        for(int i=0;i<howManyBombs;i++) {
            tempId = listOfAllNumbers.get((int) (Math.random()*listOfAllNumbers.size()));
            listOfAllNumbers.remove(Integer.valueOf(tempId));
            y = ((int)tempId/area[0].length);
            x = tempId%area[0].length;
            area[y][x] = new Area(ValueOfArea.BOMB);
        }
    }
    private static int getHowManyBombs(Area[][] area, Scanner scanner) {
        while(true) {
            System.out.println("напишите количество бомб: ");
            String howManyBombsString = scanner.nextLine();
            if(!isNumeric(howManyBombsString)) {
                System.out.println("вы должны ввести число!");
            }
            else if(!(0 < Integer.parseInt(howManyBombsString) && Integer.parseInt(howManyBombsString) < area.length * area[0].length)) {
                System.out.println("число должно быть положительным и не превышать число ячеек!");
            }
            else {
                return Integer.parseInt(howManyBombsString);
            }
        }
    }

    private static Area[][] pickLengthsOfAreaOrLoadGame(Scanner scanner) {
        String[] turnXandY;
        String answer;
        System.out.println("Хотите загрузить игру? Ответьте да или нет");
        answer = scanner.nextLine();
        while(!(answer.equals("да") || answer.equals("нет"))) {
            System.out.println("Напишите да или нет");
            answer = scanner.nextLine();
        }
        if (answer.equals("да")) {
            System.out.println("Напишите название игры");
            String saveName = scanner.nextLine() + ".mine";
            Minesweeper.isLoaded = true;
            SavedState state;
            InputStream file;
            try {
                file = new FileInputStream(saveName);
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream (buffer);
                state = (SavedState)input.readObject();
                area = state.area;
                input.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("ФАЙЛ НЕ НАЙДЕН!");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return area;
        }
        else {
            while(true) {
                System.out.println("Выберите длину и ширину поля(напишите \"x y\"): ");
                turnXandY = scanner.nextLine().split(" ");
                if(turnXandY.length != 2) {
                    System.out.println("напишите: \"x y\"!");
                }
                else if(!isNumeric(turnXandY[0]) || !isNumeric(turnXandY[1])) {
                    System.out.println("x и y должны быть числами!");
                }
                else if(Integer.parseInt(turnXandY[0]) <= 0 || Integer.parseInt(turnXandY[1]) <= 0) {
                    System.out.println("x и y должны быть >0!");
                }
                else {
                    return new Area[Integer.parseInt(turnXandY[0])][Integer.parseInt(turnXandY[1])];
                }
            }
        }
    }

    private static boolean isXandYIn(int turnX,int turnY, Area[][] area) {
        if(turnX<0 || area[0].length<=turnX) {
            return false;
        }
        if(turnY<0 || area.length<=turnY) {
            return false;
        }
        return true;
    }

    public static boolean isNumeric(String strNum) {
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }



}

class SavedState implements Serializable {
    Area[][] area;
    public SavedState(Area[][] area) {
        this.area = area;
    }
}


class Area implements Serializable {
    private final ValueOfArea valueOfArea;
    private StatusOfArea statusOfArea;

    {
        statusOfArea = StatusOfArea.CLOSED;
    }
    Area(ValueOfArea valueOfArea){
        this.valueOfArea = valueOfArea;
    }
    ValueOfArea getValueOfArea() {
        return valueOfArea;
    }
    StatusOfArea getStatusOfArea() {
        return statusOfArea;
    }
    void setStatusOfArea(StatusOfArea statusOfArea) {
        this.statusOfArea = statusOfArea;
    }
}
class AreaWithXandY{
    private int x,y;
    Area area;

    AreaWithXandY(int x,int y,Area area){
        this.area = area;
        this.x = x;
        this.y = y;
    }
    int getX() {
        return x;
    }
    int getY() {
        return y;
    }
    Area getArea() {
        return area;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj==null) {
            return false;
        }
        if(false==(obj instanceof AreaWithXandY)) {
            return false;
        }
        AreaWithXandY object = (AreaWithXandY) obj;
        if(object.getX()==x && object.getY()==y) {
            return true;
        }
        else {
            return false;
        }
    }
}
enum StatusOfArea{
    MARKEDASBOMB,OPENED,CLOSED
}
enum ValueOfArea{
    BOMB("@"),
    NOONEBOMBAROUND("0"),ONEBOMBAROUND("1"),TWOBOMBAROUND("2"),TREEBOMBAROUND("3"),FOURBOMBAROUND("4"),FIVEBOMBAROUND("5"),SIXBOMBAROUND("6"),SEVENBOMBAROUND("7"),EIGHTBOMBAROUND("8");
    private String icon;
    ValueOfArea(String icon){
        this.icon = icon;
    }
    String getIcon() {
        return icon;
    }
    static ValueOfArea getValueOfAreaByString(String value) {
        ValueOfArea[] values = ValueOfArea.values();
        for (ValueOfArea valueOfArea : values) {
            if (valueOfArea.getIcon().equals(value)) {
                return valueOfArea;
            }
        }
        System.err.println("Ошибка в getValueOfAreaByString(String value), мы не нашли такого значения");
        return null;
    }
}


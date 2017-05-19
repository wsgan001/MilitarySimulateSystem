package thu.infosecurity.simulate.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.PointLight;
import javafx.scene.shape.Circle;
import thu.infosecurity.simulate.controller.SceneCreator;
import thu.infosecurity.simulate.controller.SceneInitial;
import thu.infosecurity.simulate.model.Soldier;
import thu.infosecurity.simulate.model.Target;

import javax.management.timer.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by DaFei-PC on 2017-05-16.
 */
public class MainViewController {

    SceneInitial sc;

    @FXML
    private Button landBtn;

    @FXML
    private Button startBtn;

    @FXML
    private Button resetBtn;

    @FXML
    private Group sceneGroup;

    @FXML
    private Canvas sceneCanvas;

    @FXML
    private TextArea infoTextArea;

    @FXML
    private TextField soldierNum;

    @FXML
    private  TextField boxNum;

    private boolean isLand = false;  //表示是否点击降落按钮

    private boolean isOk = false;    //表示是否点击开始按钮

    private static final double MIN_DISTANCE = 40.0; //表示每个士兵方圆MIN_DISTANCE像素以内表示可以进行认证对话等

    private Set<Integer> arriveTargetSet = new HashSet<Integer>();  //表示已经到达box目的地的士兵id

    private boolean isOpenBox = false;  //表示打开设备箱子


//    @FXML
//    private PerspectiveCamera sceneCam;

    /**
     * The constructor (is called before the initialize()-method).
     */
    public MainViewController() {

        sc = new SceneInitial();

        arriveTargetSet.clear();
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
//        // Handle Button event.
//        startBtn.setOnAction((ActionEvent event) -> {
//            GraphicsContext gc = sceneCanvas.getGraphicsContext2D();
//            gc.setFill(Color.GREEN);
//            for(Soldier soldier: sc.getSoldierList()){
//                gc.fillOval(soldier.getPosition().getX(),soldier.getPosition().getY(),15,15);
//            }
//            gc.setStroke(Color.BLUE);
//            gc.setLineWidth(5);
//            gc.strokeRoundRect(sc.getWeaponBox().getPosition().getX(),sc.getWeaponBox().getPosition().getY(),20,20,10,10);
//        });


        //启动线程，每隔300ms更新goup
        new Thread() {
            public void run(){
                while(true)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //实时刷新
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            if(isOk){
                                runOperate();
                                showGroup();
                            }
                        }
                    });
                }
            }

        }.start();

    }


    /**
     * Handle resetBtn Button event.
     */
    @FXML
    private void handleResetBtn()
    {
        isOk = false;

        isLand = false;

        isOpenBox = false;

        soldierNum.setText("");

        sceneGroup.getChildren().clear();  //清空之前的图像

        sc = new SceneInitial();

        arriveTargetSet.clear();

    }

    /**
     * Handle startBtn Button event.
     */
    @FXML
    private void handleStartBtn()
    {
        if(isLand)
        {
            isOk = true;
        }
    }

    /**
     * Handle landBtn Button event.
     */
    @FXML
    private void handleLandBtn()
    {
        String str = soldierNum.getText();
        boolean isInt = true;
        for(int i=0; i<str.length(); i++)   //判断输入是否非数字
        {
            if(str.charAt(i) >= '0' && str.charAt(i) <= '9') {
            }
            else {
                isInt = false;
                break;
            }
        }
        if(isInt) {
            int soldierNumber = Integer.parseInt(str);
            if (soldierNumber != 0 && !str.isEmpty() && !isLand) {

                isLand = true;

                sc.initial(0, soldierNumber);

                showGroup();

            }
        }
    }

    /**
     * Handle event. File -> Quit
     */
    @FXML
    private void handleQuit()
    {
        System.exit(0);
    }

    /**
     * show all shape in group.
     */
    @FXML
    private void showGroup() {
        sceneGroup.getChildren().clear();  //清空之前的图像
        sceneGroup.setAutoSizeChildren(true);

        Label imageLableBox = new Label();
//            Image image = new Image("file:resource/image/box.png");
        Image imageBox;
        if(isOpenBox)
        {
            imageBox = new Image(this.getClass().getResourceAsStream("boxOpen.png"));
        }
        else
        {
            imageBox = new Image(this.getClass().getResourceAsStream("box.png"));
        }
        imageLableBox.setGraphic(new ImageView(imageBox));

        Label lableBox = new Label("     装备箱  ");

        VBox vb = new VBox();
        vb.setLayoutX(sc.getWeaponBox().getPosition().getX());
        vb.setLayoutY(sc.getWeaponBox().getPosition().getY());
        vb.getChildren().addAll(imageLableBox, lableBox);

//        System.out.println("Box Location: " + vb.getLayoutX() + " " + vb.getLayoutY());

        sceneGroup.getChildren().add(vb);

        int index = 0;
        for(Soldier soldier: sc.getSoldierList())
        {
            Label imageLable = new Label();
//            Image image = new Image("file:resource/image/soldier.png");
            Image image;
            if(isOpenBox && arriveTargetSet.contains(index))
            {
                image = new Image(this.getClass().getResourceAsStream("soldierOpen.png"));
            }
            else
            {
                image = new Image(this.getClass().getResourceAsStream("soldier.png"));
            }

            imageLable.setGraphic(new ImageView(image));

            Label lable = new Label(String.valueOf(index + 1));

            HBox hb = new HBox();
            if(isOpenBox && arriveTargetSet.contains(index))
            {
                hb.setLayoutX((index/3) * 40.0 + 70.0);
                hb.setLayoutY((index%3) * 30.0);
            }
            else
            {
                hb.setLayoutX(soldier.getPosition().getX());
                hb.setLayoutY(soldier.getPosition().getY());
            }
            hb.getChildren().addAll(lable, imageLable);

            sceneGroup.getChildren().add(hb);

            index ++;
        }

    }

    /**
     * 每个士兵每次向箱子走一定的距离
     */
    private void runOperate() {

        Target targetBox = sc.getWeaponBox();

        ArrayList<Soldier> soldierList = sc.getSoldierList();

        for(int i=0; i<soldierList.size(); i++)
        {
            Soldier soldier = soldierList.get(i);
            if(dis(soldier.getPosition(), targetBox.getPosition()) <= MIN_DISTANCE + 20)
            {
                arriveTargetSet.add(i);   //到达目的地box就进入集合set中
            }
        }

        //TODO 添加遍历每个士兵，周围MIN_DISTANCE距离以内表示可以进行认证操作




        for(int i=0; i<soldierList.size(); i++)
        {
            if(!arriveTargetSet.contains(i))   //没有到达box
            {
                Soldier soldier = soldierList.get(i);

                Point point = walkOne(soldier.getPosition(), targetBox.getPosition());   //走一步

                soldier.setPosition(point);
            }
            else
            {
                System.out.print(i + ",");   //打印出到达目的地box的士兵id
            }
        }
        if(!arriveTargetSet.isEmpty())
            System.out.println();

        if(arriveTargetSet.size() == sc.getSoldierList().size()) //表示士兵全部到达目的地, TODO 可能这个判断条件还要修改，开锁的条件可能是士兵数的2/3或其他
        {
            //TODO 添加打开设备box的验证操作,调用共享秘钥


            isOpenBox = true; //打开设备box成功
        }

    }

    /**
     * 计算两点间的距离
     * @param a
     * @param b
     * @return
     */
    private double dis(Point a, Point b)
    {
        return Math.sqrt((a.getX()-b.getX())*(a.getX()-b.getX()) + (a.getY()-b.getY())*(a.getY()-b.getY()));
    }

    /**
     * x向目的地box走一步，返回坐标
     * @param x
     * @param target
     * @return
     */
    private Point walkOne(Point a, Point target)
    {
        double verticalDis = MIN_DISTANCE * Math.abs(a.getY()-target.getY()) / dis(a, target);   //竖直的距离差
        double horizontalDis = MIN_DISTANCE * Math.abs(a.getX()-target.getX()) / dis(a, target);   //水平的距离差

        Point p = new Point();

        //以下在target的八个方位进行计算
        if(a.getX() <= target.getX() && a.getY() <= target.getY())  //左上
        {
            p.setLocation(a.getX() + horizontalDis, a.getY() + verticalDis);
        }
        else if(a.getX() == target.getX() && a.getY() < target.getY())  //上
        {
            p.setLocation(a.getX() + horizontalDis, a.getY() + verticalDis);
        }
        else if(a.getX() >= target.getX() && a.getY() <= target.getY())  //右上
        {
            p.setLocation(a.getX() - horizontalDis, a.getY() + verticalDis);
        }
        else if(a.getX() > target.getX() && a.getY() == target.getY())  //右
        {
            p.setLocation(a.getX() - horizontalDis, a.getY() + verticalDis);
        }
        else if(a.getX() >= target.getX() && a.getY() >= target.getY())  //右下
        {
            p.setLocation(a.getX() - horizontalDis, a.getY() - verticalDis);
        }
        else if(a.getX() == target.getX() && a.getY() > target.getY())  //下
        {
            p.setLocation(a.getX() - horizontalDis, a.getY() - verticalDis);
        }
        else if(a.getX() <= target.getX() && a.getY() >= target.getY())  //左下
        {
            p.setLocation(a.getX() + horizontalDis, a.getY() - verticalDis);
        }
        else if(a.getX() < target.getX() && a.getY() == target.getY())  //左
        {
            p.setLocation(a.getX() + horizontalDis, a.getY() - verticalDis);
        }

        return p;
    }

}

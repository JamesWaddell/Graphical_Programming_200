//
// 159.235, 2018 S2
// Name: James Waddell, ID: 16379344
// Assignment 2 - A simple CAD program.
//
//
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.io.File;


// The problem is that the scale is not actually changing the values in the matrix

public class Ass2 extends JFrame implements ActionListener, ChangeListener {

    // Menu items
    private JMenuItem openItem, quitItem, helpItem;

    // Buttons to change the size of the figure
    private JButton biggerButton, smallerButton;

    // Sliders for rotation angles
    private JSlider sliderXY, sliderXZ, sliderYZ;
    private static final int SLIDER_MIN  = 0;
    private static final int SLIDER_MAX  = 360;
    private static final int SLIDER_INIT = 0;

    // Dimensions of JFrame
    private static final int FRAME_WIDTH  = 800;
    private static final int FRAME_HEIGHT = 900;

    private Rotation rotation;
    private double matrix [][];
    private double originalMatrix [][];
    private int numOfVertices = 0;
    private int numOfIndices = 0;
    private int index1[];
    private int index2[];
    private int index3[];
    private double scale = 50.0;
    private double averageZValues[];
    private Polygon arrayOfPolygons[];
    private Integer[] indicesOfZValues;
    private double skipArray[];


    //Calculates average of 3 variables
    public double average(double zValue1, double zValue2, double zValue3){
        return ((zValue1+zValue2+zValue3)/3.0);
    }

    //Calculates the average z value for each triangle
    public void calculateAverageZValues(){
        for(int i  =0; i<numOfIndices; i++) {
            averageZValues[i] = average(matrix[index1[i]][2], matrix[index2[i]][2], matrix[index3[i]][2]);
        }
    }

    //Sorts array of average z values for calculating order to draw polygons
    public void sortArray() {
        calculateAverageZValues();
        Double[] zmeasures = new Double[numOfIndices];
        for (int n = 0; n < numOfIndices ; n++) {
            zmeasures[n] = averageZValues[n];
        }
        ArrayIndexComparator comparator = new ArrayIndexComparator(zmeasures);
        indicesOfZValues = comparator.createIndexArray();
        Arrays.sort(indicesOfZValues, comparator);
    }

    //Set the original matrix
    public void setOriginalMatrix(){
        for(int i =1; i<numOfVertices+1; i++){
            for(int j =0; j<3; j++){
                originalMatrix[i][j] = matrix[i][j];
            }
        }
    }

    // Need this for the menu items and buttons
    public void actionPerformed(ActionEvent event) {
        JComponent source = (JComponent)event.getSource();
        if (source == openItem) {
            numOfVertices = 0;
            numOfIndices = 0;
            JFileChooser chooser = new JFileChooser("./");
            int retVal = chooser.showOpenDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try{
                    Scanner scanner = new Scanner(file);
                    String line;
                    //Because the array of vertices starts at one
                    int i = 1;
                    int j = 0;
                    numOfVertices = Integer.parseInt(scanner.nextLine());
                    //Set size for the matrices(the plus 1 is because the vertex indices start at 1).
                    matrix = new double [numOfVertices+1][3];
                    originalMatrix  = new double [numOfVertices+1][3];

                    while(scanner.hasNextLine()){
                        if(i < numOfVertices+1) {
                            line = scanner.nextLine();
                            StringTokenizer strtok = new StringTokenizer(line, " \t");
                            matrix[i][0] =Double.parseDouble(strtok.nextToken());   // x
                            matrix[i][1] = Double.parseDouble(strtok.nextToken());  // y
                            matrix[i][2] = Double.parseDouble(strtok.nextToken());  // z
                        }else if(i == numOfVertices+1){
                            numOfIndices = Integer.parseInt(scanner.nextLine());
                            //Set the size for the indices
                            index1 = new int [numOfIndices+1];
                            index2 = new int [numOfIndices+1];
                            index3 = new int [numOfIndices+1];
                            //Set size for skip array
                            skipArray = new double[numOfIndices+1];
                            //Set size for average z values array
                            averageZValues = new double[numOfIndices];
                            //Set size for array of polygons
                            arrayOfPolygons = new Polygon[numOfIndices];
                        }else {
                            line = scanner.nextLine();
                            StringTokenizer strtok = new StringTokenizer(line, " \t");
                            index1[j] = Integer.parseInt(strtok.nextToken());
                            index2[j]  = Integer.parseInt(strtok.nextToken());
                            index3[j] = Integer.parseInt(strtok.nextToken());
                            j++;
                        }
                        i++;
                    }
                    scanner.close();
                    setOriginalMatrix();
                    //Set the rotation to the previous rotation
                    rotation.setXYRotationMatrix(rotation.getAngleXY());
                    rotation.setYZRotationMatrix(rotation.getAngleYZ());
                    rotation.setXZRotationMatrix(rotation.getAngleXZ());
                    repaint();

                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }

            }

        }
        else if (source == quitItem) {
            System.out.println("Quitting ...");
            System.exit(0);
        }

        else if (source == helpItem) {
            System.out.println("Help me!");
        }

        else if (source == biggerButton) {
            scale++;
            repaint();
        }
        else if (source == smallerButton) {
            if(scale>1.0) {
                scale--;
            }
            repaint();
        }
    }

    // Applies all matrix rotations
    public void applyRotations(){
        double passingMatrix1 [][] = new double [numOfVertices+1][3];
        double passingMatrix2 [][] = new double [numOfVertices+1][3];
        // Multiply  original matrix(with scale) by  XY rotation matrix
        multiply(originalMatrix, rotation.getXZRotationMatrix(), passingMatrix1);
        // Multiply result from first matrix multiplication by the YZ rotation matrix
        multiply(passingMatrix1, rotation.getYZRotationMatrix(), passingMatrix2);
        // Multiply result from second matrix multiplication by the XZ rotation matrix
        multiply(passingMatrix2, rotation.getXYRotationMatrix(), matrix);
    }

    //Calculate the cross product
    public void crossProduct(double u[], double v[], double result []){
        result[0] = ((u[1]*v[2])-(u[2]*v[1]));
        result[1] = ((u[2]*v[0])-(u[0]*v[2]));
        result[2] = ((u[0]*v[1])-(u[1]*v[0]));
    }

    //Calculate the dot product
    public double dotProduct(double n[]){
        double v[] = new double [3];
        v[0] = 0;
        v[1] = 0;
        v[2] = -1;
        return ((n[0]*v[0])+(n[1]*v[1])+(n[2]*v[2]));

    }

    //Applies the back-face culling method to the triangles
    public void applyBackFaceCulling(){
        double n[] = new double [3];
        double result;
        double vector1[] = new double[3];
        double vector2[] = new double[3];
        for(int i =0; i<numOfIndices; i++) {
            //Find the vector from point 1 to point 2
            vector1[0] = matrix[index2[i]][0] - matrix[index1[i]][0];
            vector1[1] = matrix[index2[i]][1] - matrix[index1[i]][1];
            vector1[2] = matrix[index2[i]][2] - matrix[index1[i]][2];
            //Find the vector from point 2 to point 3
            vector2[0] = matrix[index3[i]][0] - matrix[index2[i]][0];
            vector2[1] = matrix[index3[i]][1] - matrix[index2[i]][1];
            vector2[2] = matrix[index3[i]][2] - matrix[index2[i]][2];
            //Calculate the cross product of those vectors
            crossProduct(vector1, vector2, n);
            //Calculate the dot product of the normal and (0,0,-1).
            result = dotProduct(n);
            if(result>=0){
                //Set the current position in the skip array to 1 which
                //  will be used to tell the polygon array not to draw
                //  a triangle at that position.
                skipArray[i] = 1;
            }else{
                skipArray[i] = 0;
            }
        }
    }

    // Need this for the sliders
    public void stateChanged(ChangeEvent e) {
        repaint();
        JSlider source = (JSlider)e.getSource();
        if (source == sliderXY) {
            rotation.setAngleXY(Math.toRadians((double)source.getValue()));
            rotation.setXYRotationMatrix(rotation.getAngleXY());
        }else if(source == sliderYZ){
            rotation.setAngleYZ(Math.toRadians((double)source.getValue()));
            rotation.setYZRotationMatrix(rotation.getAngleYZ());
        }else if(source == sliderXZ){
            rotation.setAngleXZ(Math.toRadians((double)source.getValue()));
            rotation.setXZRotationMatrix(rotation.getAngleXZ());
        }
    }

    //Creates the menu
    public void makeMenu() {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        openItem = new JMenuItem("Open");
        quitItem = new JMenuItem("Quit");
        fileMenu.add(openItem);
        fileMenu.add(quitItem);

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        helpItem = new JMenuItem("Help");
        helpMenu.add(helpItem);

        openItem.addActionListener(this);
        quitItem.addActionListener(this);
        helpItem.addActionListener(this);
    }

    //Creates a new JSlider
    JSlider makeSlider(JPanel panel, String heading) {
        JSlider slider = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
        slider.setBorder(BorderFactory.createTitledBorder(heading));
        slider.addChangeListener(this);
        slider.setMajorTickSpacing(90);
        slider.setMinorTickSpacing(30);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        panel.add(slider);
        return slider;

    }

    //Multiples two matrices together
    public void multiply(double[][] mx1, double[][] mx2,
                         double[][] result) {
        double sum;
        for (int i=1; i<numOfVertices+1; ++i) {
            for (int j=0; j<3; ++j) {
                sum = 0;
                for (int k=0; k <3; ++k) {
                    sum += mx1[i][k] * mx2[k][j];
                }
                result[i][j] = sum;
            }
        }
    }

    //Class Ass2 constructor
    public Ass2() {
        super("159235 Assignment 2");
        rotation = new Rotation();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);

        makeMenu();

        // Get a reference to the JFrames content pane to which
        // JPanels will be added
        Container content = this.getContentPane();
        content.setLayout(null);

        DisplayPanel Dpanel = new DisplayPanel();
        Dpanel.setBounds(new Rectangle(0, 100, FRAME_WIDTH, FRAME_HEIGHT));
        content.add(Dpanel);
        // Make a control panel for the sliders and buttons using a JPanel
        JPanel controlP = new JPanel();
        controlP.setBounds(new Rectangle(0, 0, 800, 100));

        content.add(controlP);
        sliderXY = makeSlider(controlP, "XY Plane");
        sliderYZ = makeSlider(controlP, "YZ Plane");
        sliderXZ = makeSlider(controlP, "XZ Plane");

        biggerButton = new JButton("Bigger");
        smallerButton = new JButton("Smaller");
        controlP.add(biggerButton);
        controlP.add(smallerButton);
        biggerButton.addActionListener(this);
        smallerButton.addActionListener(this);

        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setVisible(true);
        //Set the angles to 0 when starting up
        // rotation.setAngles(0.0);
        rotation.setAngleXY(0.0);
        rotation.setAngleYZ(0.0);
        rotation.setAngleXZ(0.0);

    }

    // An inner class to handle the final rendering of the figure
    class DisplayPanel extends JPanel {
        public DisplayPanel() {}

        //Draws things in the window
        public void paintComponent( Graphics g ){
            applyRotations();
            sortArray();
            Graphics2D g2 = (Graphics2D)g;
            g2.translate(FRAME_WIDTH/2, 350);
            g2.scale(1,-1);
            applyBackFaceCulling();
            int xdraw [] = new int [3];
            int ydraw [] = new int [3];
            for(int i = 0; i < numOfIndices; i++) {
                xdraw[0] = (int) (matrix[index1[i]][0]*scale);
                xdraw[1] = (int) (matrix[index2[i]][0]*scale);
                xdraw[2] = (int) (matrix[index3[i]][0]*scale);
                ydraw[0] = (int) (matrix[index1[i]][1]*scale);
                ydraw[1] = (int) (matrix[index2[i]][1]*scale);
                ydraw[2] = (int) (matrix[index3[i]][1]*scale);
                arrayOfPolygons[i] = new Polygon(xdraw, ydraw, 3);
            }
            int pos;
            int counter = 0;
            for(int i =0; i<numOfIndices; i ++){
                pos = indicesOfZValues[i];
                if(skipArray[pos] == 0){
                    g2.setColor(Color.gray);
                    g2.fill(arrayOfPolygons[pos]);
                    g2.setColor(Color.darkGray);
                    g2.draw(arrayOfPolygons[pos]);
                    counter++;
                }
            }
            //Scale and translate back to original state for info
            g2.scale(1,-1);
            g2.translate(-(FRAME_WIDTH/2), -350);
            g2.setColor(Color.RED);
            g2.drawString("[Removed "+(numOfIndices-counter)+" from "+numOfIndices+" triangles]", 10, 10);
            g2.setColor(Color.blue);
            g2.drawString("[Number of triangles drawn: "+counter+"]", 10, 30);
        }
    }

    //A class used to create an array index comparator
    class ArrayIndexComparator implements Comparator<Integer> {
        private final Double[] array;

        public ArrayIndexComparator(Double[] array){
            this.array = array;
        }

        public Integer[] createIndexArray(){
            Integer[] indices = new Integer[array.length];
            for (int i = 0; i < array.length; i++){
                indices[i] = i; // Autoboxing
            }
            return indices;
        }

        public int compare(Integer i1, Integer i2){
            // Auto-unbox from Integer to int to use as array indices
            return array[i1].compareTo(array[i2]);
        }
    }

    //A class used for rotations around different axes
    class Rotation{
        private double xyRotationMatrix[][] = new double [3][3];
        private double yzRotationMatrix[][] = new double [3][3];
        private double xzRotationMatrix[][] = new double [3][3];
        private double angleXY;
        private double angleYZ;
        private double angleXZ;

        //Gets XY angle
        public double getAngleXY() {
            return angleXY;
        }

        //Sets XY angle
        public void setAngleXY(double angleXY) {
            this.angleXY = angleXY;
        }

        //Gets YZ angle
        public double getAngleYZ() {
            return angleYZ;
        }

        //Sets YZ angle
        public void setAngleYZ(double angleYZ) {
            this.angleYZ = angleYZ;
        }
        //Gets XZ angle
        public double getAngleXZ() {
            return angleXZ;
        }

        //Sets XZ angle
        public void setAngleXZ(double angleXZ) {
            this.angleXZ = angleXZ;
        }

        //Gets the XY rotation matrix
        public double [][] getXYRotationMatrix(){
            return xyRotationMatrix;
        }

        //Sets the XY rotation matrix
        public void setXYRotationMatrix(double angle){
            xyRotationMatrix[0][0] = Math.cos(angle);
            xyRotationMatrix[0][1] = -(Math.sin(angle));
            xyRotationMatrix[0][2] = 0.0;
            xyRotationMatrix[1][0] = Math.sin(angle);
            xyRotationMatrix[1][1] = Math.cos(angle);
            xyRotationMatrix[1][2] = 0.0;
            xyRotationMatrix[2][0] = 0.0;
            xyRotationMatrix[2][1] = 0.0;
            xyRotationMatrix[2][2] = 1.0;

        }

        //Gets the YZ rotation matrix
        public double [][] getYZRotationMatrix(){
            return yzRotationMatrix;
        }

        //Sets the YZ rotation matrix
        public void setYZRotationMatrix(double angle){
            yzRotationMatrix[0][0] = 1.0;
            yzRotationMatrix[0][1] = 0.0;
            yzRotationMatrix[0][2] = 0.0;
            yzRotationMatrix[1][0] = 0.0;
            yzRotationMatrix[1][1] = Math.cos(angle);
            yzRotationMatrix[1][2] = -(Math.sin(angle));
            yzRotationMatrix[2][0] = 0.0;
            yzRotationMatrix[2][1] = Math.sin(angle);
            yzRotationMatrix[2][2] = Math.cos(angle);
        }

        //Gets the XZ rotation matrix
        public double [][] getXZRotationMatrix(){
            return xzRotationMatrix;
        }

        //Sets the XZ rotation matrix
        public void setXZRotationMatrix(double angle){
            xzRotationMatrix[0][0] = Math.cos(angle);
            xzRotationMatrix[0][1] = 0.0;
            xzRotationMatrix[0][2] = Math.sin(angle);
            xzRotationMatrix[1][0] = 0.0;
            xzRotationMatrix[1][1] = 1.0;
            xzRotationMatrix[1][2] = 0.0;
            xzRotationMatrix[2][0] = -(Math.sin(angle));
            xzRotationMatrix[2][1] = 0.0;
            xzRotationMatrix[2][2] = Math.cos(angle);
        }
    }


    // Program entry point
    public static void main(String[] args) {
        new Ass2();
    }

}
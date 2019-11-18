//==========================================================\\
// BodeZ class, extends JApplet
// Originated 11/8/2014 by M. Williamsen
// Plots complex frequency response from Z-domain
// transfer function coefficients in entry fields.
// Will multiply lists of polynomials and collect terms.
// May be run in a browser, appletviewer, or standalone.
// Implements Java Swing user interface.
// Version 1.0.1, 12/23/2014
// Based on BodeNyquist Applet version 2.0
// http://www.williamsonic.com

package BodeZPackage;

//==========================================================\\
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

// units of measure for horizontal axis
enum FreqUnits {radSamp, cycSamp, radSec, cycSec};

// main class for this applet
//==========================================================\\
public class BodeZ extends JApplet
{
    // information strings
    private static final String versionStr  =
    "Z-Domain Bode/Nyquist Plot v. 1.0.1;  M. Williamsen 12/23/2014";
    private static final String infoStr[][] =
    {
        {"numerator",   "String",      "Polynomial coefficients or list of polynomials"},
        {"denominator", "String",      "Polynomial coefficients or list of polynomials"},
        {"startFreq",   "Real number", "Frequency at left edge of plot"},
        {"decades",     "Integer",     "Number of decades to plot"},
        {"units",       "String",      "Units of frequency"},
        {"sampRate",    "Real number", "Samples per second"},
    };
    
    // GUI components
    PlotCanvas    theCanvas;
    PolarCanvas   thePolar;
    ImpulseCanvas theImpulse;
    PlotPanel     thePanel;
    PlotData      theData;
    JTabbedPane   thePane;
    
    // instance variables
    String numStr;    // numerator coefficients
    String denStr;    // denominator coefficients
    String startStr;  // plot start frequency
    String sampStr;   // sample rate
    String decadStr;  // number of decades to plot
    String unitsStr;  // units of frequency
    
    // Set look & feel to match local OS, before drawing anything.
    private void setLAF()
    {
        String theLAF = UIManager.getSystemLookAndFeelClassName();
        // System.out.println("Local look & feel: " + theLAF);
        try{UIManager.setLookAndFeel(theLAF);}
        catch(UnsupportedLookAndFeelException e)
            {System.out.println("UnsupportedLookAndFeelException.");}
        catch(Exception e)
            {System.out.println("Failed to set Look And Feel.");}
    }
    
    // default constructor, needed for browsers and appletviewer
    public BodeZ()
    {
        // Set the local look & feel before drawing anything.
        setLAF();
    }

    // construct BodeZ instance for standalone execution
    public BodeZ(String args[])
    {
        // preset to default values (notch filter 0.142797 rad/samp)
        numStr   = "0.00439456;(1,2,1)";
        denStr   = "1,-1.734834,0.752412";
        startStr = "100";
        decadStr = "2";
        unitsStr = "cyc/sec";
        sampStr  = "44100";

        // accept command line arguments if present
        switch (args.length)
        {
        case 6:
            sampStr  = args[5];
        case 5:
            unitsStr = args[4];
        case 4:
            decadStr = args[3];
        case 3:
            startStr = args[2];
        case 2:
            denStr   = args[1];
        case 1:
            numStr   = args[0];
            break;
        default:
            System.out.println("usage: java -cp BodeZ.jar BodeZ numCoeff [denCoeff [startFreq [2|3|4 [units [sampRate]]]]]");
            break;
        }

        // Set look & feel to match local OS.
        setLAF();        
    }

    // main entry point for standalone execution
    public static void main(String args[])
    {
        // construct an instance of this applet
        final BodeZ thePlot = new BodeZ(args);
        thePlot.init();

        // construct window frame to run applet
        JFrame theFrame = new JFrame("Z-Domain Bode/Nyquist Plot");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.getContentPane().add(thePlot);
        theFrame.setSize(755, 630);
        theFrame.setVisible(true);
        thePlot.start();
    }
        
    // initialization code
    public void init()
    {
        // obtain applet parameters from HTML
        if (null == numStr)     {numStr   = getParameter("numerator");}
        if (null == denStr)     {denStr   = getParameter("denominator");}
        if (null == startStr)   {startStr = getParameter("startFreq");}
        if (null == decadStr)   {decadStr = getParameter("decades");}
        if (null == unitsStr)   {unitsStr = getParameter("units");}
        if (null == sampStr)    {sampStr  = getParameter("sampRate");}

        // Show version info on console
        System.out.println(versionStr);
        
        // add GUI component for plot area
        getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER));
        theCanvas  = new PlotCanvas(this);
        thePolar   = new PolarCanvas(this);
        theImpulse = new ImpulseCanvas(this);
        
        // set up tabbed pane
        thePane = new JTabbedPane();
        thePane.setOpaque(false);
        thePane.addTab("BodeZ Plot", theCanvas);
        thePane.addTab("Nyquist", thePolar);
        thePane.addTab("Impulse", theImpulse);
        thePanel = new PlotPanel(this);
        getContentPane().add(thePane);
        getContentPane().add(thePanel);
    }
    
    // start the applet
    public void start()
    {
        // draw plot for the first time
        thePanel.doPlot();
    }

    // implement some applet methods    
    public String getAppletInfo(){return versionStr;}
    public String[][] getParameterInfo(){return infoStr;}
}

//==========================================================\\
// component subclass to hold Bode plot area
class PlotCanvas extends JPanel
{
    // instance data members
    private final BodeZ theApp;

    // constructor with one arg, a reference to the parent
    PlotCanvas(BodeZ anApp)
    {
        // keep reference to parent
        theApp = anApp;
        
        // required to allow look and feel to show through
        setOpaque(false);
        setPreferredSize(new Dimension(725, 325));
    }

    // draw the plot area as needed
    public void paint(Graphics g)
    {
        if (null != theApp.theData)
            {theApp.theData.paintPlot(g);}
    }    
}

//==========================================================\\
// component subclass to hold Nyquist (polar) plot
class PolarCanvas extends JPanel
{
    // instance data members
    private final BodeZ theApp;
    
    // constructor with one arg, a reference to the parent
    PolarCanvas(BodeZ anApp)
    {
        // keep reference to parent
        theApp = anApp;
        
        // required to allow look and feel to show through
        setOpaque(false);
        setPreferredSize(new Dimension(725, 325));
    }
    
    // draw the plot area as needed
    public void paint(Graphics g)
    {
        if (null != theApp.theData)
            {theApp.theData.paintPolar(g);}
    }    
}

//==========================================================\\
// component subclass to hold impulse and step function plots
class ImpulseCanvas extends JPanel
{
    // instance data members
    private final BodeZ theApp;
    
    // constructor with one arg, a reference to the parent
    ImpulseCanvas(BodeZ anApp)
    {
        // keep reference to parent
        theApp = anApp;
        
        // required to allow look and feel to show through
        setOpaque(false);
        setPreferredSize(new Dimension(725, 325));
    }
    
    // draw the plot area as needed
    public void paint(Graphics g)
    {
        if (null != theApp.theData)
        {theApp.theData.paintImpulse(g);}
    }
}

//==========================================================\\
// component to hold GUI controls
class PlotPanel extends JPanel
implements ActionListener, ChangeListener
{
    // reference to the applet
    private final BodeZ theApp;

    // Swing instance variables, to support platform-based look & feel.
    final JButton theButton;
    final JCheckBox theCheck;
    final JLabel numLabel;
    final JTextField numField;
    final JLabel denLabel;
    final JTextField denField;
    final JLabel startLabel;
    final JTextField startField;
    final JTextArea resultsArea;
    final JComboBox decadesCombo;
    final JComboBox unitsCombo;
    final JTextField sampField;
    final JLabel sampLabel;

    // constructor with one argument, a reference to the parent
    PlotPanel(BodeZ anApp)
    {
        theApp = anApp;
        setPreferredSize(new Dimension(725, 212));
        setLayout(new GridLayout(1,2,8,8));

        // required to allow look and feel to show through
        setOpaque(false);
        
        // button control to plot new data
        theButton = new JButton("Plot Response");
        theButton.setOpaque(false);
        theButton.addActionListener(this); // register for events
        
        // check box control to inhibit phase plot
        theCheck = new JCheckBox("Hide Phase", false);
        theCheck.setOpaque(false);
        theCheck.addActionListener(this); // register for events
        
        // choice control for decades
        String decadesList[] = {"2 Decades", "3 Decades", "4 Decades"};
        decadesCombo = new JComboBox(decadesList);
        decadesCombo.setOpaque(false);
        int numDecades = 2;
        try{numDecades = Integer.parseInt(theApp.decadStr);}
        catch(NumberFormatException e)
        {
            System.out.println("Couldn't parse number of decades: "
             + theApp.decadStr);
            numDecades = 2;
        }
        
        // limit range for number of decades to (2...4) 
        numDecades = Math.min(numDecades, 4);
        numDecades = Math.max(numDecades, 2);
        decadesCombo.setSelectedIndex(numDecades-2);
        decadesCombo.addActionListener(this); // register for events

        // choice control for units
        String unitsList[] = {"rad/samp", "cyc/samp", "rad/sec", "cyc/sec"};
        unitsCombo = new JComboBox(unitsList);
        unitsCombo.setOpaque(false);
        if (null != theApp.unitsStr) {unitsCombo.setSelectedItem(theApp.unitsStr);}
        unitsCombo.addActionListener(this); // register for events
        
        // set up button controls
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(theButton);
        buttonPanel.add(theCheck);
        
        // set up drop down controls
        JPanel dropPanel = new JPanel();
        dropPanel.setOpaque(false);
        dropPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        dropPanel.add(decadesCombo);
        dropPanel.add(unitsCombo);
        
        // text entry field for numerator coefficients
        numField = new JTextField(theApp.numStr, 18);
        numLabel = new JLabel("Numerator", Label.LEFT);
        numLabel.setOpaque(false);
        JPanel numPanel = new JPanel();
        numPanel.setOpaque(false);
        numPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        numPanel.add(numField);
        numPanel.add(numLabel);

        // text entry field for denominator coefficients
        denField = new JTextField(theApp.denStr, 18);
        denLabel = new JLabel("Denominator", Label.LEFT);
        denLabel.setOpaque(false);
        JPanel denPanel = new JPanel();
        denPanel.setOpaque(false);
        denPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        denPanel.add(denField);
        denPanel.add(denLabel);
        
        // text entry field for start frequency of plot
        startField = new JTextField(theApp.startStr, 18);
        startLabel = new JLabel("Start Freq.", Label.LEFT);
        startLabel.setOpaque(false);

        // panel to hold start frequency
        JPanel startPanel = new JPanel();
        startPanel.setOpaque(false);
        startPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        startPanel.add(startField);
        startPanel.add(startLabel);
        
        // test entry field for samples per second
        sampField = new JTextField(theApp.sampStr, 18);
        sampLabel = new JLabel("Samples/Sec.", Label.LEFT);
        sampLabel.setOpaque(false);
        
        // panel to hold samples per second entry field
        JPanel sampPanel = new JPanel();
        sampPanel.setOpaque(false);
        sampPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        sampPanel.add(sampField);
        sampPanel.add(sampLabel);
        
        // set up overall control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        controlPanel.setLayout(new GridLayout(6,1));
        controlPanel.setBorder(BorderFactory.createEtchedBorder());
        add(controlPanel);
        controlPanel.add(buttonPanel);
        controlPanel.add(dropPanel);
        controlPanel.add(numPanel);
        controlPanel.add(denPanel);
        controlPanel.add(startPanel);
        controlPanel.add(sampPanel);
        theApp.thePane.addChangeListener(this);
        
        // static text field for transfer function results
        resultsArea = new JTextArea("Transfer function results appear here.");
        resultsArea.setEditable(false);
        resultsArea.setBorder(BorderFactory.createEtchedBorder());
        add(new JScrollPane(resultsArea));
    }
    
    // handle button click events
    public void actionPerformed(ActionEvent e)
    {
        // checkbox doesn't need to recalculate
        if (e.getSource() == theCheck)
        {
            theApp.theCanvas.repaint();
            theApp.thePolar.repaint();
            theApp.theImpulse.repaint();
        }
        
        // other controls must recalculate
        else {doPlot();}
    }
    
    // handle tab pane changes
    public void stateChanged(ChangeEvent e)
    {
        StringBuffer theBuff;
        int theTab = theApp.thePane.getSelectedIndex();
        switch (theTab)
        {
            case 0:
			case 1: theBuff = theApp.theData.getResult(); break;
            case 2: theBuff = theApp.theData.getImpulse(); break;
            default: theBuff = new StringBuffer("Unexpected tab index:" + theTab); break;
        }
        if (null == theBuff)
        {
            System.out.println("Error: failed to plot data.");
            resultsArea.setText("Error: failed to plot data.");
            return;
        }
        resultsArea.setText(theBuff.toString());
    }
    
    void doPlot()
    {
        // build polynomial objects for numerator and denominator
        double[] numCoeff;
        double[] denCoeff;
        double startFreq;
        double sampRate;
        
        // get user input from numerator text field
        theApp.numStr = numField.getText();
        try{numCoeff = Polynomial.convertPolyList(theApp.numStr);}
        catch(NumberFormatException e)
        {
            resultsArea.setText("Couldn't parse numerator: " + theApp.numStr);
            System.out.println("Couldn't parse numerator: " + theApp.numStr);
            return;
        }
        
        // get user input from denominator text field
        theApp.denStr = denField.getText();
        try{denCoeff = Polynomial.convertPolyList(theApp.denStr);}
        catch(NumberFormatException e)
        {
            resultsArea.setText("Couldn't parse denominator: "+theApp.denStr);
            System.out.println("Couldn't parse denominator: "+theApp.denStr);
            return;
        }
        if ((1 == denCoeff.length) && (0 == denCoeff[0]))
        {
            resultsArea.setText("Denominator can't be zero: "+theApp.denStr);
            System.out.println("Denominator can't be zero: "+theApp.denStr);
            return;
        }
        
        // get user input from start freq text field
        theApp.startStr = startField.getText();
        try{startFreq = Double.parseDouble(theApp.startStr);}
        catch(NumberFormatException e)
        {
            resultsArea.setText("Couldn't parse start freq: "+theApp.startStr);
            System.out.println("Couldn't parse start freq: "+theApp.startStr);
            return;
        }
        if (0.0 == startFreq)
        {
            resultsArea.setText("Start freq can't be zero: "+theApp.startStr);
            System.out.println("Start freq can't be zero: "+theApp.startStr);
            return;
        }
        
        // get user input from samples per second text field
        theApp.sampStr = sampField.getText();
        try{sampRate = Double.parseDouble(theApp.sampStr);}
        catch(NumberFormatException e)
        {
            resultsArea.setText("Couldn't parse sample rate: "+theApp.sampStr);
            System.out.println("Couldn't parse sample rate: "+theApp.sampStr);
            return;
        }
        if (0.0 == sampRate)
        {
            resultsArea.setText("Sample rate can't be zero: "+theApp.sampStr);
            System.out.println("Sample rate can't be zero: "+theApp.sampStr);
            return;
        }
        
        // check units of frequency
        FreqUnits unitsRad = FreqUnits.radSamp;
        int theItem = theApp.thePanel.unitsCombo.getSelectedIndex();
		switch (theItem)
		{
		case 0: unitsRad = FreqUnits.radSamp; break;
		case 1: unitsRad = FreqUnits.cycSamp; break;
		case 2: unitsRad = FreqUnits.radSec; break;
		case 3: unitsRad = FreqUnits.cycSec; break;
		default: unitsRad = FreqUnits.radSamp; break;
		}

        // if no problems, show results on console
        System.out.print(">>Numerator: ");
        Polynomial.showArray(numCoeff);
        System.out.print("Denominator: ");
        Polynomial.showArray(denCoeff);
		System.out.print("Start freq.: " + startFreq);
		switch (theItem)
		{
		case 0: System.out.println(" rad/samp"); break;
		case 1: System.out.println(" cyc/samp"); break;
		case 2: System.out.println(" rad/sec"); break;
		case 3: System.out.println(" cyc/sec"); break;
		default: System.out.println(" (unknown units)"); break;
		}
        System.out.println("Sample rate: " + sampRate  + ' ' +"samp/sec");
        
        // calculate transfer function results
        theApp.theData = new PlotData(theApp, startFreq, unitsRad, numCoeff, denCoeff, sampRate);
        stateChanged(null);
                
        // ask plots to redraw themselves with new data
        theApp.theCanvas.repaint();
        theApp.thePolar.repaint();
        theApp.theImpulse.repaint();
    }    
}

//==========================================================\\
// container for complex frequency response
class PlotData
{
    private final BodeZ theApp;
    final double startFreq;
    final FreqUnits unitsRad;
    final int theRange;
    final int numDecades;
    final double sampRate;

    Complex[] theResult;
    double[] theFreqs;
    double maxReal;
    double minReal;
    double maxImag;
    double minImag;
    
    double[] impulseOutput;
    double[] stepFcnOutput;
    double maxImpulse;
    double minImpulse;
    double maxStepFcn;
    double minStepFcn;
    
    // constructor with args
    PlotData(BodeZ anApp, double aFreq, FreqUnits theUnits, double[] numCoeff, double[] denCoeff, double aRate)
    {
        // copy input parameters
        theApp = anApp;
        startFreq = aFreq;
        unitsRad = theUnits;
        sampRate = aRate;

        // check number of decades from combo control
        int theItem = theApp.thePanel.decadesCombo.getSelectedIndex();
		switch (theItem)
		{
		case 0: theRange = 300; numDecades = 2; break;
		case 1: theRange = 200; numDecades = 3; break;
		case 2: theRange = 150; numDecades = 4; break;
		default: theRange = 300; numDecades = 2; break;
		}
        
        // initialize list of frequencies to plot
        theFreqs = new double[601];
        theFreqs[0] = startFreq;
        switch (unitsRad)
        {
            case cycSamp: theFreqs[0] *= (2. * Math.PI); break;
            case radSec:  theFreqs[0] /= sampRate; break;
            case cycSec:  theFreqs[0] /= (sampRate / 2. / Math.PI); break;
        }
        int index;
        for (index = 1; index < theFreqs.length; index++)
        {
            // find next frequency in radians/sample as a ratio
            theFreqs[index] = Math.pow(10.0, 1.0/theRange)*theFreqs[index-1]; 
        }
        
        // calculate complex response at each plot frequency
        computePlot(numCoeff, denCoeff);
        computeImpulse(numCoeff, denCoeff);
    }
    
    StringBuffer getResult()
    {
        // copy transfer function results to text buffer
        StringBuffer theBuff;
        theBuff = new StringBuffer("Freq. ");
        switch (unitsRad)
        {
            case radSamp: theBuff.append("(rad/samp)"); break;
            case cycSamp: theBuff.append("(cyc/samp)");  break;
            case radSec:  theBuff.append("(rad/sec)"); break;
            case cycSec:  theBuff.append("(cyc/sec)");  break;
        }
        theBuff.append("\tComplex Resp.\n");
        
        // always use scientific notation with 6 sig. figs.
        final DecimalFormat theFormat = new DecimalFormat("#.######E0");
        int index;
        for (index = 0; index < theResult.length; index++)
        {   
            // first column in frequency in chosen units
            double aFreq = theFreqs[index];
            switch (unitsRad)
            {
                case cycSamp: aFreq /= (2. * Math.PI); break;
                case radSec:  aFreq *= sampRate; break;
                case cycSec:  aFreq *= (sampRate / 2. / Math.PI); break;
            }
            theBuff.append(theFormat.format(aFreq));
            
            // second column is complex response
            theBuff.append('\t');
            theBuff.append(theResult[index].toString());
            theBuff.append('\n');
        }
        return theBuff;
    }

    StringBuffer getImpulse()
    {
        // scientific notation, 6 sig. figs.
        final DecimalFormat theFormat = new DecimalFormat("#.######E0");
        StringBuffer theBuff;
        theBuff = new StringBuffer("Time (sec)\tImpulse Response\tStep Function\n");
        int index;
        for (index = 0; index < impulseOutput.length; index++)
        {
			theBuff.append(theFormat.format(index / sampRate));
			theBuff.append('\t');
            theBuff.append(theFormat.format(impulseOutput[index]));
            theBuff.append('\t');
            theBuff.append(theFormat.format(stepFcnOutput[index]));
            theBuff.append('\n');
        }
        return theBuff;
    }
    
    // draw Bode plot in first tabbed pane
    void paintPlot(Graphics g)
    {
        final boolean hidePhase = theApp.thePanel.theCheck.isSelected();
        
        // move origin to make room for text labels
        g.translate(50, 3);
        
        // draw white rectangle for plot area
        g.setColor(Color.white);
        g.fillRect(0, 0, 599, 299);
        
        // draw log frequency scale
        final DecimalFormat theFormat = new DecimalFormat("#.####");
        double aFreq;
        int index;
        int outer;
        int inner = 0;
        for (outer = 1; outer < 10; outer++)
        {
            for (inner = 0; inner < numDecades; inner++)
            {   
                // draw vertical grid lines
                int gridx = (int) (Math.log(outer) * theRange / Math.log(10.0) + 0.5 + inner * theRange);
                aFreq = outer * startFreq * Math.pow(10.,inner);
                g.setColor(Color.lightGray);
                g.drawLine(gridx, 0, gridx, 299);
                g.setColor(Color.black);
                
                // draw text labels as needed
                switch (numDecades)
                {
                    // handle 4 decades plot
                case 4:
                    switch (outer)
                    {
                    case 1:
                    case 3:
                        g.drawLine(gridx, 299, gridx, 304);
                        g.drawString(theFormat.format(aFreq), gridx, 317);
                        break;
                    default:
                        break;
                    }
                    break;
                        
                    // handle 3 decades plot
                case 3:
                    switch (outer)
                    {
                    case 1:
                    case 2:
                    case 5:
                        g.drawLine(gridx, 299, gridx, 304);
                        g.drawString(theFormat.format(aFreq), gridx, 317);
                        break;
                    default:
                        break;
                    }
                    break;
                            
                    // handle 2 decades plot
                case 2:
                    switch (outer)
                    {
                    case 1:
                    case 2:
                    case 4:
                    case 6:
                        g.drawLine(gridx, 299, gridx, 304);
                        g.drawString(theFormat.format(aFreq), gridx, 317);
                        break;
                    default:
                        break;
                    }
                    break;
                          
                    // should never reach here
                default:
                    break;
                }
            }
        }
        
        // special case for last label on the right
        g.drawLine(599, 299, 599, 304);
        aFreq = startFreq * Math.pow(10.,inner);
        g.drawString(theFormat.format(aFreq), 599, 317);
        
        // draw linear dB scale
        for (index = 1; index < 10; index++)
        {
            // draw horizontal grid lines
            g.setColor(Color.lightGray);
            int gridy = index * 30;
            g.drawLine(0, gridy, 599, gridy);
            
            // draw phase labels if not hidden
            if (!hidePhase)
            {
                g.setColor(Color.black);
                g.drawString(String.valueOf(225-(index*45)), 610, gridy+4);
            }
        }
        
        // draw legend
        g.setColor(Color.black);
        switch (unitsRad)
        {
            case radSamp: g.drawString("Freq.  (rad/samp)", 267, 290); break;
            case cycSamp: g.drawString("Freq.  (cyc/samp)", 267, 290); break;
            case radSec:  g.drawString("Freq.  (rad/sec)",  267, 290); break;
            case cycSec:  g.drawString("Freq.  (cyc/sec)",  267, 290); break;
        }
        
        g.setColor(Color.blue);
        g.drawString("Gain (dB)", 10, 18);
        if (!hidePhase)
        {
            g.setColor(Color.magenta);
            g.drawString("Phase (deg)", 520, 18);
        }
        
        // draw outline rectangle on top of everything
        g.setColor(Color.black);
        g.drawRect(0, 0, 599, 299);
        
        // check that data exists before plotting magnitude
        if ((null == theResult) || (0 == theResult.length))
        {
            System.out.println("Error: nothing to plot.");
            return;
        }
        
        // convert all data points to dB
        double[] dBResult = new double[theResult.length];
        double maxDB = -100.0;
        for (index = 0; index < theResult.length; index++)
        {
            // convert magnitude to dB, scaled for 5 pixels/dB
            double theMag = theResult[index].mod();
            theMag = 20.0 * Math.log(theMag) / Math.log(10.0);
            dBResult[index] = theMag;
            
            // save maximum dB value
            maxDB = Math.max(maxDB, theMag);
        }
        
        // limit vertical display to the range (-90...90)
        maxDB = Math.max(Math.min(maxDB, 90), -90);
        
        // snap to vertical grid, draw text labels
        maxDB = 6.0 * Math.round(maxDB/6.0);
        for (index = 0; index < 9; index++)
        {
            g.drawString(String.valueOf((int)(maxDB - (index * 6.))),
                         -35, index*30 + 34);
        }
        
        // reduce clip area while plotting
        g.clipRect(1, 1, 599, 299);
        
        // plot phase response first if not hidden
        int oldy = 0;
        int newy = 0;
        int oldx = 0;
        if (!hidePhase)
        {
            g.setColor(Color.magenta);
            for (index = 0; index < theResult.length; index++)
            {
                // check Nyquist limit
                if (theFreqs[index] >= Math.PI) {g.setColor(Color.lightGray);}

                // plot on canvas scaled to 300 pixel height
                newy = (int) (150.5-(theResult[index].arg()*120./Math.PI));
                if (0 == index){oldy = newy;}
                else {g.drawLine(oldx, oldy, (int)(index), newy);}
                oldy = newy;
                oldx = index;
            }
        }
        
        // plot magnitude response last
        oldy = 0;
        newy = 0;
        oldx = 0;
        g.setColor(Color.blue);
        for (index = 0; index < theResult.length; index++)
        {
            // check Nyquist limit
            if (theFreqs[index] >= Math.PI) {g.setColor(Color.gray);}

            // plot on canvas scaled to 300 pixel height
            newy = (int) (5.0 * (maxDB - dBResult[index] + 6.) + 0.5);
            if (0 == index){oldy = newy;}
            else {g.drawLine(oldx, oldy, (int)(index), newy);}
            oldy = newy;
            oldx = index;
        }
    }
    
    // draw Nyquist plot in second tabbed pane
    void paintPolar(Graphics g)
    {
        final boolean hidePhase = theApp.thePanel.theCheck.isSelected();

        // move origin to make room for text labels
        g.translate(40, 3);
        
        // scale plot to fit width and height
        double xfactor = Math.max(maxReal, -minReal);
        double yfactor = Math.max(maxImag, -minImag);
        double preFactor = Math.max(0.5 * xfactor, yfactor);
        preFactor = Math.max(1.e-6, preFactor);
        
        // find a 'nice' number to set scale factor
        double mult = Math.log(preFactor) / Math.log(10.);
        mult = Math.pow(10., Math.round(mult - 1.5));
        preFactor = mult * Math.round(preFactor / mult);
        final int scale = 13;
        double factor = 10. * scale / preFactor;
        
        // draw white rectangle for plot area
        g.setColor(Color.white);
        g.fillRect(0, 0, 599, 299);
        
        // draw horiz and vert axes
        g.setColor(Color.lightGray);
        g.drawLine(0, 150, 604, 150);
        g.drawLine(300, 0, 300, 304);
    
        // draw ticks and grid lines for vertical axis
        final DecimalFormat theFormat = new DecimalFormat("#.######");
        int index;
        for (index = -10; index <= 10; index++)
        {
            if (0 == index % 10)
            {
                g.setColor(Color.gray);
                g.drawLine(294,150+index*scale,306,150+index*scale);
                g.setColor(Color.black);
                g.drawLine(600,150+index*scale,604,150+index*scale);
                String theStr = theFormat.format(index*preFactor/10.);
                g.drawString(theStr, 610, 154-index*scale);
            }
            else if (0 == index % 5)
            {
                g.setColor(Color.gray);
                g.drawLine(296,150+index*scale,304,150+index*scale);
                g.drawLine(600,150+index*scale,604,150+index*scale);
            }
            else
            {
                g.setColor(Color.lightGray);
                g.drawLine(298,150+index*scale,302,150+index*scale);
            }
        }
        
        // draw ticks and grid lines for horizontal axis
        for (index = -21; index <= 21; index++)
        {
            g.setColor(Color.lightGray);
            if (0 == index % 10)
            {
                g.setColor(Color.gray);
                g.drawLine(300+index*scale,144,300+index*scale,156);
                g.setColor(Color.black);
                g.drawLine(300+index*scale,300,300+index*scale,304);
                String theStr = theFormat.format(index*preFactor/10.);
                g.drawString(theStr, 300+index*scale, 317);
            }
            else if (0 == index % 5)
            {
                g.setColor(Color.gray);
                g.drawLine(300+index*scale,146,300+index*scale,154);
            }
            else
            {
                g.setColor(Color.lightGray);
                g.drawLine(300+index*scale,148,300+index*scale,152);
            }
        }
        
        // draw outline rectangle on top
        g.setColor(Color.black);
        g.drawRect(0, 0, 599, 299);
        g.drawString("Imaginary Plane", 256, 15);

        // if data exists, plot magnitude
        if ((theResult == null) || (theResult.length == 0))
        {
            System.out.println("Error: nothing to plot.");
            return;
        }

        // reduce clip area while plotting
        g.clipRect(1, 1, 598, 298);

        // draw text label for first point
        int newx = 0;
        int newy = 0;
        int oldx = 0;
        int oldy = 0;
                        
        // plot negative frequencies if enabled
        if (!hidePhase)
        {
            // draw unit circle
            int tickx = (int)(300.5 - factor);
            int dia = (int)(2. * factor);
            g.setColor(Color.lightGray);
            g.drawOval(tickx, tickx-150, dia, dia);
            g.setColor(Color.magenta);
            g.drawLine(tickx, 142, tickx, 158);
            
            // plot response
            boolean first = true;
            for (index = theResult.length-1; index >= 0; index--)
            {
                // check Nyquist limit
                if (theFreqs[index] < Math.PI) {g.setColor(Color.magenta);}
                else {g.setColor(Color.lightGray);}
                newx = (int)(300.5 + factor * theResult[index].real());
                newy = (int)(150.5 + factor * theResult[index].imag());
                if (first)
                {   // special handling for first point
                    oldx = newx;
                    oldy = newy;
                    first = false;
                }
                else {g.drawLine(oldx, oldy, newx, newy);}
                oldx = newx;
                oldy = newy;
            }
        }

        // plot continuous curve
        boolean first = true;
        for (index = theResult.length-1; index >= 0; index--)
        {
            newx = (int)(300.5 + factor * theResult[index].real());
            newy = (int)(150.5 - factor * theResult[index].imag());
            if (first)
            {   // special handling for first point, text label
                oldx = newx;
                oldy = newy;

                // check Nyquist limit
                if (theFreqs[index] < Math.PI) {g.setColor(Color.black);}
                else {g.setColor(Color.gray);}
                g.drawString("\u03C9", newx+3, newy-3);
                first = false;
            }
            else
            {
                // check Nyquist limit
                if (theFreqs[index] < Math.PI) {g.setColor(Color.blue);}
                else {g.setColor(Color.gray);}
                g.drawLine(oldx, oldy, newx, newy);
                oldx = newx;
                oldy = newy;
            }
        }

        // draw text label for last point
        g.setColor(Color.black);
        g.drawString("\u03B1", newx-12, newy-3);
    }
    
    void paintImpulse(Graphics g)
    {
        final boolean hidePhase = theApp.thePanel.theCheck.isSelected();
        
        // move origin to make room for text labels
        g.translate(40, 3);

        // scale plot to fit width and height
        double maxResp = maxImpulse;
        double minResp = minImpulse;
        if (!hidePhase)
        {
            maxResp = Math.max(maxImpulse, maxStepFcn);
            minResp = Math.min(minImpulse, minStepFcn);
        }
        double yFactor = Math.max(maxResp, -minResp);
        double preFactor = Math.max(1.e-6, yFactor);
        
        // find a 'nice' number to set scale factor
        double mult = Math.log(preFactor) / Math.log(10.);
        mult = Math.pow(10., Math.round(mult - 1.5));
        preFactor = mult * Math.round(preFactor / mult);
        final int scale = 12;
        double factor = 10. * scale / preFactor;
        
        // draw white rectangle for plot area
        g.setColor(Color.white);
        g.fillRect(0, 0, 599, 299);
        
        // draw ticks and ticks and labels for horizontal axis
        final DecimalFormat theFormat = new DecimalFormat("#.####");
		g.setColor(Color.black);
        int index;
        for (index = 0; index < 501; index += 100)
        {
            g.drawLine(index+30, 299, index+30, 304);
            String theStr = theFormat.format(index / sampRate);
            g.drawString(theStr, index+34, 317);
        }

        // draw ticks and labels for vertical axis
        for (index = -10; index <= 10; index += 5)
        {
            if (0 == index % 10)
            {
                g.setColor(Color.black);
                g.drawLine(600,150+index*scale,604,150+index*scale);
                String theStr = theFormat.format(index*preFactor/10.);
                g.drawString(theStr, 610, 154-index*scale);
            }
        }
        
        // draw cross marks at grid points
        g.setColor(Color.lightGray);
        int outer, inner;
        for (outer = 30; outer < 600; outer += 100)
        {
            for (inner = -10; inner <= 10; inner += 5)
            {
                g.drawLine(outer-3,150+inner*scale,outer+3,150+inner*scale);
                g.drawLine(outer,150+inner*scale-3,outer,150+inner*scale+3);
            }
        }
        
        // draw grid lines through origin
        g.setColor(Color.lightGray);
        g.drawLine(30,0,30,299);
        g.drawLine(0,150,599,150);
        
        // draw outline rectangle and text legend
        g.setColor(Color.black);
        g.drawRect(0, 0, 599, 299);
		g.drawString("Time (sec)", 250, 290);
        g.setColor(Color.blue);
        g.drawString("Impulse Response", 10, 18);
        if (!hidePhase)
        {
            g.setColor(Color.magenta);
            g.drawString("Step Function", 500, 18);
        }
        
        // done if no data
        if ((null == impulseOutput) || (0 == impulseOutput.length))
        {
            System.out.println("Error: no impulse response to plot.");
            return;
        }
        else if ((null == stepFcnOutput) || (0 == stepFcnOutput.length))
        {
            System.out.println("Error: no step function to plot.");
            return;
        }
        
        // reduce clip area while plotting
        g.clipRect(1, 1, 598, 298);
        int oldx = 0;
        int oldy = 150;
        int newx = 30;
        int newy = 150;
        g.setColor(Color.blue);
        g.drawLine(oldx, oldy, newx, newy);
        oldx = newx;
        oldy = newy;
        for (index = 0; index < impulseOutput.length; index++)
        {
            // plot impulse response
            newx = (int)index + 32;
            newy = (int)(150.5 - factor*impulseOutput[index]);
            g.drawLine(oldx, oldy, newx, newy);
            oldx = newx;
            oldy = newy;
        }
        oldx = 0;
        oldy = 150;
        newx = 30;
        newy = 150;
        if (!hidePhase)
        {
            // plot step function response
            g.setColor(Color.magenta);
            g.drawLine(oldx, oldy, newx, newy);
            oldx = newx;
            oldy = newy;
            for (index = 0; index < stepFcnOutput.length; index++)
            {
                newx = (int)index + 32;
                newy = (int)(150.5 - factor*stepFcnOutput[index]);
                g.drawLine(oldx, oldy, newx, newy);
                oldx = newx;
                oldy = newy;
            }
        }
    }

    // calculate the complex reponses to all frequencies in input list
    private void computePlot(double[] nums, double[] dens)
    {
        // initialize extremes
        maxReal = -1.e10;
        minReal =  1.e10;
        maxImag = -1.e10;
        minImag =  1.e10;
        
        // check for empty arrays
        theResult = null;
        if (0 == nums.length) {return;}
        if (0 == dens.length) {return;}
        if (0 == theFreqs.length) {return;}
        
        // allocate memory for result array
        theResult = new Complex[theFreqs.length];
        
        // loop through frequencies in sequence
        int outer;
        for (outer = 0; outer < theFreqs.length; outer++)
        {
            // create complex representation of frequency in Z-domain
            Complex cFreq = Complex.exp(new Complex(0, -theFreqs[outer]));
            
            // initialize multiplier to unity
            Complex cMult = new Complex(1, 0);
            
            // calculate complex numerator using all coefficients
            Complex cNum = new Complex(0, 0);
            int inner;
            for (inner = 0; inner < nums.length; inner++)
            {
                // get next numerator coefficient
                Complex cCoef = new Complex(nums[inner], 0);
                
                // multiply complex frequency, add to result
                cCoef.mpy(cMult);
                cNum.add(cCoef);
                
                // increase order of complex frequency
                cMult.mpy(cFreq);
            }
            
            // reset multiplier to unity
            cMult = new Complex(1, 0);
            
            // calculate complex denominator using all coefficients
            Complex cDen = new Complex(0, 0);
            for (inner = 0; inner < dens.length; inner++)
            {
                // get next denominator coefficient
                Complex cCoef = new Complex(dens[inner], 0);
                
                // multiply complex frequency, add to result
                cCoef.mpy(cMult);
                cDen.add(cCoef);
                
                // increase order of complex frequency
                cMult.mpy(cFreq);
            }
            
            // divide numerator by denominator
            theResult[outer] = cNum.div(cDen);
            
            // check for extreme values at each frequency
            double realVal = theResult[outer].real();
            double imagVal = theResult[outer].imag();
            maxReal = Math.max(maxReal, realVal);
            minReal = Math.min(minReal, realVal);
            maxImag = Math.max(maxImag, imagVal);
            minImag = Math.min(minImag, imagVal);
        }
    }
    
    private void computeImpulse(double[] nums, double[] dens)
    {
        // initialize extremes
        maxImpulse = -1.e10;
        minImpulse =  1.e10;
        maxStepFcn = -1.e10;
        minStepFcn =  1.e10;
        
        // check for empty arrays
        if (0 == nums.length) {return;}
        if (0 == dens.length) {return;}
        if (0.== dens[0])     {return;}
        
        // initialize arrays with driving functions
        double[] impulseInput = new double[512];
        double[] stepFcnInput = new double[512];
        impulseOutput = new double[512];
        stepFcnOutput = new double[512];
        int outer;
        for (outer = 0; outer < impulseInput.length; outer++)
        {
            if (0 == outer) {impulseInput[outer] = 1.;}
            else {impulseInput[outer] = 0.;}
            stepFcnInput[outer] = 1.;
        }

        // iterate over samples and coefficient
        // to obtain output using Direct Form I
        for (outer = 0; outer < impulseOutput.length; outer++)
        {
            int inner;
            for (inner = 0; inner < nums.length; inner++)
            {
                final int offset = outer - inner;
                if (offset < 0) {continue;}
                impulseOutput[outer] += nums[inner] * impulseInput[offset] / dens[0];
                stepFcnOutput[outer] += nums[inner] * stepFcnInput[offset] / dens[0];
            }
            for (inner = 1; inner < dens.length; inner++)
            {
                final int offset = outer - inner;
                if (offset < 0) {continue;}
                impulseOutput[outer] -= dens[inner] * impulseOutput[offset] / dens[0];
                stepFcnOutput[outer] -= dens[inner] * stepFcnOutput[offset] / dens[0];
            }
            
            // update min and max at each sample
            if (outer < 500)
            {
                maxImpulse = Math.max(maxImpulse, impulseOutput[outer]);
                minImpulse = Math.min(minImpulse, impulseOutput[outer]);
                maxStepFcn = Math.max(maxStepFcn, stepFcnOutput[outer]);
                minStepFcn = Math.min(minStepFcn, stepFcnOutput[outer]);
            }
        }
    }
}

//==========================================================\\
// complex values for transfer functions
class Complex
{
    // real part of number
    private double x;

    // imaginary part of number
    private double y;

    // default constructor
    Complex()
    {
        x = 0.0;    // initialize real component
        y = 0.0;    // initialize imaginary component
    }
    
    // constructor with arguments
    Complex(double r, double i)
    {
        x = r;    // set real component
        y = i;    // set imaginary component
    }

    // return real part of complex number
    double real(){return x;}

    // return imaginary part of complex number
    double imag(){return y;}

    // return the modulus (magnitude) of complex number
    double mod(){return Math.sqrt(x * x + y * y);}

    // return the argument (phase angle in radians) of complex number
    double arg(){return Math.atan2(y, x);}

    // add c to this, return this
    Complex add(Complex c)
    {
        x += c.x;
        y += c.y;
        return this;
    }

    // multiply this by c, return this
    Complex mpy(Complex c)
    {
        double x_new = x*c.x - y*c.y;
        double y_new = x*c.y + y*c.x;

        // move result to this
        x = x_new; y = y_new;
        return this;
    }

    // divide this by c, return this
    Complex div(Complex c)
    {
        // rationalize to make denominator real
        double den = c.x*c.x + c.y*c.y;

        // apply result
        double x_new = (x*c.x + y*c.y)/den;
        double y_new = (c.x*y - c.y*x)/den;

        // move result to this
        x = x_new; y = y_new;
        return this;
    }

    // take exponential of c, return c
    static Complex exp(Complex c)
    {
        double x_new = Math.exp(c.x) * Math.cos(c.y);
        double y_new = Math.exp(c.x) * Math.sin(c.y);
        
        // move result to this
        c.x = x_new; c.y = y_new;
        return c;
    }
    
    // obtain string representation of complex number
    // designed to be compatible with MS Excel
    public String toString()
    {
        // use scientific notation to 6 sig. figs.
        final DecimalFormat theFormat = new DecimalFormat("+#.######E0;-#");
        String result = new String(theFormat.format(x)
            + theFormat.format(y) + "i");
        return result;
    }
}

//==========================================================\\
// this class holds only static methods, no instance data
class Polynomial
{
    // convert list of coefficients from input string
    // to an array of doubles
    static double[] convertPoly(String theStr)
    {
        // obtain numerator coefficients from text field
        theStr = theStr.replace(',', ' ');
        theStr = theStr.trim();
        StringTokenizer theToken = new StringTokenizer(theStr);
        double[] theCoeff = new double[theToken.countTokens()];

        // load coefficients into array
        int index;
        for (index = 0; index < theCoeff.length; index++)
        {
            theCoeff[index] = Double.parseDouble(theToken.nextToken());
        }
        return theCoeff;
    }

    // convert list of polynomials from input string
    // to an array of doubles
    static double[] convertPolyList(String theStr)
    {
        // initialize return value to unity
        double[] theReply = {1};

        // polynomials separated by ']' characters
        theStr = theStr.replace('(', ' ');
        theStr = theStr.replace('[', ' ');
        theStr = theStr.replace(')', ';');
        theStr = theStr.replace(']', ';');
        theStr = theStr.trim();
        StringTokenizer theToken = new StringTokenizer(theStr, ";");
        int index;
        for (index =  0; theToken.countTokens() > 0; index++)
        {
            double[] theArray = convertPoly(theToken.nextToken());
            theReply = multiply(theReply, theArray);
        }
        return theReply;
    }

    static double[] multiply(double[] array1, double[] array2)
    {
        double[] theReply = new double[array1.length + array2.length - 1];
        int outer;
        for (outer = 0; outer < array1.length; outer++)
        {
            int inner;
            for (inner = 0; inner < array2.length; inner++)
            {
                theReply[outer + inner] += (array1[outer] * array2[inner]);
            }
        }
        return theReply;
    }

    static void showArray(double[] theArray)
    {
        boolean first = true;
        final DecimalFormat theFormat = new DecimalFormat("#.######");
        int index;
        for (index = 0; index < theArray.length; index++)
        {
            if (!first){System.out.print(", ");}
            System.out.print(theFormat.format(theArray[index]));
            first = false;
        }
        System.out.println();
    }
}

markdown file describing how to use BodeZ to obtain the figures in my IEEE paper

Figure 5 in my paper shows two Bode plots of the gain and phase response of a sine and cosine inner product matched filters, cast as finite impulse response (FIR) filters in the Z domain. The plots were obtained using the Java application BodeZ in this repository. Follow these steps to obtain the same plots:

- Build and run the Java application
- Obtain 100 samples representing a single cycle of a sine wave, offset by one half sample so that the average of the samples is zero. Which is easily done by running the following command line:
    python3 -c "import math; print(*(math.sin((x+0.5) / 50 * math.pi) for x in range (0, 100)), sep = '\n')"
- Copy and paste all 100 samples into the "Numerator" field of the BodeZ app, replacing the previous contents.
- Type the number "50" into the "Denominator" field, replacing the previous contents.
- Set the "Start Freq." field to 0.001
- Set the units control to "cyc/samp"
- Click the "Plot Response" button to redraw the image.

You should now see the sine matched filter response, hitting unity gain (0 dB) at 0.01 cycles/sample, with deep notches at all harmonics, and a slope on the left falling at 6 dB/octave.

- Now obtain 100 samples representing a single cycle of a cosine wave, offset by one half sample so that the average of the samples is zero. Which is easily done by running the following command line:
    python3 -c "import math; print(*(math.cos((x+0.5) / 50 * math.pi) for x in range (0, 100)), sep = '\n')"
- Copy and paste all 100 samples into the "Numerator" field of the BodeZ app, replacing the previous contents.
- Click the "Plot Response" button to redraw the image.

It's ok to run two copies of the BodeZ app, to compare the two filters side-by-side. Just be sure to populate all the fields in both copies. The cosine matched filter response also hits unity gain (0 dB) at 0.01 cycles/sample, and has deep notches at all harmonics. The difference is that now the slope on the left falls off at 12 dB/octave.

markdown file describing how to build BodeZ

MarksiMac:Projects williamm$ git clone https://github.com/springleik/BodeZ.git
Cloning into 'BodeZ'...
remote: Enumerating objects: 30, done.
remote: Counting objects: 100% (30/30), done.
remote: Compressing objects: 100% (27/27), done.
remote: Total 30 (delta 6), reused 4 (delta 0), pack-reused 0 (from 0)
Receiving objects: 100% (30/30), 79.36 KiB | 3.61 MiB/s, done.
Resolving deltas: 100% (6/6), done.
MarksiMac:Projects williamm$ cd BodeZ
MarksiMac:BodeZ williamm$ javac BodeZ.java
Note: BodeZ.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
MarksiMac:BodeZ williamm$ java BodeZ
usage: java -cp BodeZ.jar BodeZ numCoeff [denCoeff [startFreq [2|3|4 [units [sampRate]]]]]
Z-Domain Bode/Nyquist Plot v. 1.0.1;  M. Williamsen 12/23/2014
>>Numerator: 0.004395, 0.008789, 0.004395
Denominator: 1, -1.734834, 0.752412
Start freq.: 100.0 cyc/sec
Sample rate: 44100.0 samp/sec
MarksiMac:BodeZ williamm$

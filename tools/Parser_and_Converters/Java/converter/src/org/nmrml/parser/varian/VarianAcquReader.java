/*
 * Copyright (c) 2013. EMBL, European Bioinformatics Institute
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nmrml.parser.varian;

import org.nmrml.parser.Acqu;
import org.nmrml.parser.Proc;

import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

/**
 * Reader for Varian's propar files
 *
 * @author Daniel Jacob
 *
 * Date: 21/07/2014
 * Time: 15:00
 *
 */
public class VarianAcquReader implements AcquReader {

    private BufferedReader inputAcqReader;

    // parameter types
    private final static Pattern REGEXP_INTEGER = Pattern.compile("1 (\\d+)"); // Get an intéger
    private final static Pattern REGEXP_DOUBLE = Pattern.compile("1 (\\d+\\.?\\d?)"); // Get a double
    private final static Pattern REGEXP_STRING = Pattern.compile("1 \"(\\S+)\""); // Get a string

    // parameters from procpar
    private final static Pattern REGEXP_SFO1 = Pattern.compile("^sfrq "); //irradiation frequency
    private final static Pattern REGEXP_BF1 = Pattern.compile("^reffrq "); //spectral frequency
    private final static Pattern REGEXP_NUMBEROFSCANS = Pattern.compile("^nt "); //number of scans
    private final static Pattern REGEXP_DUMMYSCANS = Pattern.compile("^ss "); //number of dummy (steady state) scans
    private final static Pattern REGEXP_SPINNINGRATE = Pattern.compile("^spin "); // spinning rate
    private final static Pattern REGEXP_RELAXATIONDELAY = Pattern.compile("^d1 "); // relaxation delay D1
    private final static Pattern REGEXP_TD = Pattern.compile("^np "); //acquired points (real+imaginary)
    private final static Pattern REGEXP_PULSEWIDTH = Pattern.compile("^pw "); // pulseWidth90
    private final static Pattern REGEXP_SW = Pattern.compile("^sw "); //spectral width (Hz)
    private final static Pattern REGEXP_NUC1 = Pattern.compile("^tn "); // observed nucleus
    private final static Pattern REGEXP_TEMPERATURE = Pattern.compile("^temp "); // temperature in Kelvin
    private final static Pattern REGEXP_SOLVENT = Pattern.compile("^solvent "); // solvent name
    private final static Pattern REGEXP_PROBHD = Pattern.compile("^probe_ "); // probehead
    private final static Pattern REGEXP_PULPROG = Pattern.compile("^seqfil "); //pulse program


    public VarianAcquReader() {
    }
    
    public VarianAcquReader(File acquFile) throws IOException {
        inputAcqReader = new BufferedReader(new FileReader(acquFile));
    }
    
    public VarianAcquReader(InputStream acqFileInputStream) {
        inputAcqReader = new BufferedReader(new InputStreamReader(acqFileInputStream));
    }

    public VarianAcquReader(String filename) throws IOException {
        this(new File(filename));
        // required parameters so far...
        // AquiredPoints: FidReader
        // SpectraWidth: FidReader
        // transmiterFreq: FidReader
        //
    }

    @Override
    public Acqu read() throws Exception {
        Matcher matcher;
        Locale.setDefault(new Locale("en", "US"));
        Acqu acquisition = new Acqu(Acqu.Spectrometer.VARIAN);
        String line = inputAcqReader.readLine();
        while (inputAcqReader.ready() && (line != null)) {
            //spectral frequency
            if (REGEXP_BF1.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_DOUBLE.matcher(line).find()) {
                    matcher = REGEXP_DOUBLE.matcher(line);
                    matcher.find();
                    acquisition.setSpectralFrequency(Double.parseDouble(matcher.group(1)));
                }
            }
            //irradiation_frequency
            if (REGEXP_SFO1.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_DOUBLE.matcher(line).find()) {
                    matcher = REGEXP_DOUBLE.matcher(line);
                    matcher.find();
                    acquisition.setTransmiterFreq(Double.parseDouble(matcher.group(1)));
                }
            }
            /* sweep width in Hertz*/
            if (REGEXP_SW.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_DOUBLE.matcher(line).find()) {
                    matcher = REGEXP_DOUBLE.matcher(line);
                    matcher.find();
                    acquisition.setSpectralWidthHz(Double.parseDouble(matcher.group(1)));
                }
            }
            /* number of data points */
            if (REGEXP_TD.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_INTEGER.matcher(line).find()) {
                    matcher = REGEXP_INTEGER.matcher(line);
                    matcher.find();
                    acquisition.setAquiredPoints(Integer.parseInt(matcher.group(1)));
                }
            }
            // number of scans
            if (REGEXP_NUMBEROFSCANS.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_INTEGER.matcher(line).find()) {
                    matcher = REGEXP_INTEGER.matcher(line);
                    matcher.find();
                    acquisition.setNumberOfScans(BigInteger.valueOf(Long.parseLong(matcher.group(1))));
                }
            }
            // number of dummy (steady state) scans
            if (REGEXP_DUMMYSCANS.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_INTEGER.matcher(line).find()) {
                    matcher = REGEXP_INTEGER.matcher(line);
                    matcher.find();
                    acquisition.setNumberOfSteadyStateScans(BigInteger.valueOf(Long.parseLong(matcher.group(1))));
                }
            }
            // spinning rate
            if (REGEXP_SPINNINGRATE.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_INTEGER.matcher(line).find()) {
                    matcher = REGEXP_INTEGER.matcher(line);
                    matcher.find();
                    acquisition.setSpiningRate(Integer.parseInt(matcher.group(1)));
                }
            }
            // relaxation delay D1
            if (REGEXP_RELAXATIONDELAY.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_DOUBLE.matcher(line).find()) {
                    matcher = REGEXP_DOUBLE.matcher(line);
                    matcher.find();
                    acquisition.setRelaxationDelay(Double.parseDouble(matcher.group(1)));
                }
            }
            // pulseWidth
            if (REGEXP_PULSEWIDTH.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_DOUBLE.matcher(line).find()) {
                    matcher = REGEXP_DOUBLE.matcher(line);
                    matcher.find();
                    acquisition.setPulseWidth(Double.parseDouble(matcher.group(1)));
                }
            }
            // temperature in Kelvin
            if (REGEXP_TEMPERATURE.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_DOUBLE.matcher(line).find()) {
                    matcher = REGEXP_DOUBLE.matcher(line);
                    matcher.find();
                    acquisition.setTemperature(Double.parseDouble(matcher.group(1)) + 274.15 );
                }
            }
            // solvent name
            if (REGEXP_SOLVENT.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_STRING.matcher(line).find()) {
                    matcher = REGEXP_STRING.matcher(line);
                    matcher.find();
                    acquisition.setSolvent(matcher.group(1));
                }
            }
            // observed nucleus
            if (REGEXP_NUC1.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_STRING.matcher(line).find()) {
                    matcher = REGEXP_STRING.matcher(line);
                    matcher.find();
                    acquisition.setObservedNucleus(matcher.group(1));
                }
            }
            // probehead
            if (REGEXP_PROBHD.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_STRING.matcher(line).find()) {
                    matcher = REGEXP_STRING.matcher(line);
                    matcher.find();
                    acquisition.setProbehead(matcher.group(1));
                }
            }
            // pulse program
            if (REGEXP_PULPROG.matcher(line).find()) {
                line = inputAcqReader.readLine();
                if (REGEXP_STRING.matcher(line).find()) {
                    matcher = REGEXP_STRING.matcher(line);
                    matcher.find();
                    acquisition.setPulseProgram(matcher.group(1));
                }
            }


            line = inputAcqReader.readLine();
        }

        /* sweep width in ppm*/
        acquisition.setSpectralWidth( acquisition.getSpectralWidthHz()/acquisition.getTransmiterFreq());

        inputAcqReader.close();
        return acquisition;
    }

}

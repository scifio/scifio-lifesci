package io.scif.lifesci;

import static org.junit.Assert.assertEquals;

import io.scif.formats.AbstractFormatTest;
import io.scif.img.IO;
import io.scif.util.ImageHash;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;

import org.junit.Test;
import org.scijava.io.http.HTTPLocation;
import org.scijava.io.location.FileLocation;

public class SDTFormatTest extends AbstractFormatTest {

	private static final String hash_one = "4b55c68f0eb579d841af77e11ce9c4b1a1c50e8d";

	public SDTFormatTest() throws MalformedURLException, URISyntaxException {
//		super(new HTTPLocation(""));
		super (new FileLocation("/home/gabriel/Desktop/input/sdt.zip"));
	}
	
	@Test
	public void basicReadingTest() {
		String meta = "{\"sdtInfo\":{\"width\":128,\"height\":128,\"timeBins\":64,\"channels\":16,\"timepoints\":1,\"revision\":588,\"infoOffs\":42,\"infoLength\":354,\"setupOffs\":396,\"setupLength\":16,\"dataBlockOffs\":623,\"noOfDataBlocks\":1,\"dataBlockLength\":33554432,\"measDescBlockOffs\":412,\"noOfMeasDescBlocks\":1,\"measDescBlockLength\":211,\"headerValid\":21845,\"reserved1\":1,\"reserved2\":0,\"chksum\":62464,\"info\":\"*IDENTIFICATION\\r\\n  ID        : \\u0004SPC Setup \\u0026 Data File\\u0004                     \\r\\n  Title     :                         \\r\\n  Version   : 1  781 M\\r\\n  Revision  : 8 bits ADC\\r\\n  Date      : 11:28:2003\\r\\n  Time      : 13:22:11\\r\\n  Author    : Unknown          \\r\\n  Company   : Unknown          \\r\\n  Contents  :                                                \\r\\n*END\\r\\n\\r\\n\",\"setup\":\"*SETUP\\r\\n*END\\r\\n\\r\\n\",\"hasMeasureInfo\":true,\"time\":\"13:22:14\",\"date\":\"11:28:2007\",\"modSerNo\":\"3B0027\",\"measMode\":9,\"cfdLL\":-121.56863,\"cfdLH\":80.0,\"cfdZC\":-8.3149605,\"cfdHF\":5.0,\"synZC\":-12.850393,\"synFD\":1,\"synHF\":4.0,\"tacR\":5.0E-8,\"tacG\":4,\"tacOF\":1.9291819,\"tacLL\":13.333333,\"tacLH\":97.254906,\"adcRE\":64,\"ealDE\":0,\"ncx\":1,\"ncy\":1,\"page\":1,\"colT\":60.0,\"repT\":60.0,\"stopt\":1,\"overfl\":78,\"useMotor\":0,\"steps\":1,\"offset\":0.0,\"dither\":256,\"incr\":1,\"memBank\":0,\"modType\":\"SPC-830\",\"synTH\":-19.607843,\"deadTimeComp\":0,\"polarityL\":1,\"polarityF\":1,\"polarityP\":0,\"linediv\":2,\"accumulate\":0,\"flbckY\":0,\"flbckX\":0,\"bordU\":0,\"bordL\":0,\"pixTime\":1.0E-5,\"pixClk\":0,\"trigger\":0,\"scanX\":128,\"scanY\":128,\"scanRX\":16,\"scanRY\":1,\"fifoTyp\":0,\"epxDiv\":4,\"modTypeCode\":830,\"modFpgaVer\":0,\"overflowCorrFactor\":0.0,\"adcZoom\":0,\"cycles\":1,\"hasMeasStopInfo\":false,\"status\":0,\"flags\":0,\"stopTime\":0.0,\"curStep\":0,\"curCycle\":0,\"curPage\":0,\"minSyncRate\":0.0,\"minCfdRate\":0.0,\"minTacRate\":0.0,\"minAdcRate\":0.0,\"maxSyncRate\":0.0,\"maxCfdRate\":0.0,\"maxTacRate\":0.0,\"maxAdcRate\":0.0,\"mReserved1\":0,\"mReserved2\":0.0,\"hasMeasFCSInfo\":false,\"chan\":0,\"fcsDecayCalc\":0,\"mtResol\":0,\"cortime\":0.0,\"calcPhotons\":0,\"fcsPoints\":0,\"endTime\":0.0,\"overruns\":0,\"fcsType\":0,\"crossChan\":0,\"mod\":0,\"crossMod\":0,\"crossMtResol\":0,\"hasExtendedMeasureInfo\":false,\"imageX\":0,\"imageY\":0,\"imageRX\":0,\"imageRY\":0,\"xyGain\":0,\"masterClock\":0,\"adcDE\":0,\"detType\":0,\"xAxis\":0,\"hasMeasHISTInfo\":false,\"fidaTime\":0.0,\"fildaTime\":0.0,\"fidaPoints\":0,\"fildaPoints\":0,\"mcsTime\":0.0,\"mcsPoints\":0,\"blockNo\":1,\"dataOffs\":645,\"nextBlockOffs\":33555077,\"blockType\":19,\"measDescBlockNo\":0,\"lblockNo\":1,\"blockLength\":33554432},\"binOffset\":645,\"timeBins\":64,\"channels\":16,\"mergeIntensity\":false,\"timeBase\":12.500000146076218,\"filtered\":false,\"datasetName\":\"DCIS-01-at-780-nm.sdt\",\"table\":{\"bhfileHeader.infoOffs\":42,\"Company\":\"Unknown\",\"bhfileHeader.infoLength\":354,\"MeasureInfo.repT\":60.0,\"MeasureInfo.bordU\":0,\"MeasureInfo.epxDiv\":4,\"MeasureInfo.incr\":1,\"BHFileBlockHeader.nextBlockOffs\":33555077,\"bhfileHeader.reserved2\":0,\"MeasureInfo.adcZoom\":0,\"bhfileHeader.reserved1\":1,\"MeasureInfo.colT\":60.0,\"MeasureInfo.memBank\":0,\"MeasureInfo.overflowCorrFactor\":0.0,\"time bins\":64,\"MeasureInfo.ncx\":1,\"MeasureInfo.ncy\":1,\"MeasureInfo.tacG\":4,\"Version\":\"1  781 M\",\"MeasureInfo.pixClk\":0,\"MeasureInfo.overfl\":78,\"MeasureInfo.bordL\":0,\"MeasureInfo.cfdLL\":-121.56863,\"MeasureInfo.cfdHF\":5.0,\"ID\":\"SPC Setup \\u0026 Data File\",\"MeasureInfo.cfdLH\":80.0,\"MeasureInfo.deadTimeComp\":0,\"bhfileHeader.chksum\":62464,\"MeasureInfo.scanY\":128,\"MeasureInfo.synZC\":-12.850393,\"MeasureInfo.tacR\":5.0E-8,\"MeasureInfo.scanX\":128,\"MeasureInfo.tacOF\":1.9291819,\"MeasureInfo.tacLL\":13.333333,\"MeasureInfo.modType\":\"SPC-830\",\"bhfileHeader.dataBlockOffs\":623,\"MeasureInfo.useMotor\":0,\"MeasureInfo.tacLH\":97.254906,\"MeasureInfo.modTypeCode\":830,\"MeasureInfo.date\":\"11:28:2007\",\"MeasureInfo.measMode\":9,\"MeasureInfo.ealDE\":0,\"BHFileBlockHeader.measDescBlockNo\":0,\"Contents\":\"\",\"Revision\":\"8 bits ADC\",\"channels\":16,\"MeasureInfo.synFD\":1,\"MeasureInfo.cycles\":1,\"Author\":\"Unknown\",\"MeasureInfo.accumulate\":0,\"BHFileBlockHeader.blockNo\":1,\"MeasureInfo.pixTime\":1.0E-5,\"MeasureInfo.polarityP\":0,\"MeasureInfo.trigger\":0,\"MeasureInfo.polarityL\":1,\"MeasureInfo.fifoTyp\":0,\"MeasureInfo.polarityF\":1,\"Time\":\"13:22:11\",\"MeasureInfo.steps\":1,\"MeasureInfo.synTH\":-19.607843,\"bhfileHeader.measDescBlockLength\":211,\"MeasureInfo.cfdZC\":-8.3149605,\"MeasureInfo.page\":1,\"BHFileBlockHeader.blockType\":19,\"MeasureInfo.modFpgaVer\":0,\"bhfileHeader.setupOffs\":396,\"MeasureInfo.scanRX\":16,\"bhfileHeader.revision\":588,\"MeasureInfo.scanRY\":1,\"BHFileBlockHeader.blockLength\":33554432,\"bhfileHeader.measDescBlockOffs\":412,\"MeasureInfo.stopt\":1,\"bhfileHeader.noOfMeasDescBlocks\":1,\"BHFileBlockHeader.dataOffs\":645,\"BHFileBlockHeader.lblockNo\":1,\"Title\":\"\",\"bhfileHeader.dataBlockLength\":33554432,\"MeasureInfo.modSerNo\":\"3B0027\",\"time base\":12.500000146076218,\"bhfileHeader.headerValid\":21845,\"Date\":\"11:28:2003\",\"MeasureInfo.time\":\"13:22:14\",\"MeasureInfo.linediv\":2,\"MeasureInfo.synHF\":4.0,\"MeasureInfo.flbckX\":0,\"MeasureInfo.flbckY\":0,\"bhfileHeader.noOfDataBlocks\":1,\"MeasureInfo.adcRE\":64,\"MeasureInfo.offset\":0.0,\"MeasureInfo.dither\":256},\"priority\":0.0}";
		testImg(baseFolder().child("DCIS-01-at-780-nm.sdt"), hash_one, meta, new
		int[] {128,128,64,16}, Axes.X, Axes.Y, Axes.get("Lifetime"), Axes.get("Spectra"));
	}
}

/*
 * #%L
 * SCIFIO Life Sciences Extension
 * %%
 * Copyright (C) 2013 - 2016 Open Microscopy Environment:
 * 	- Board of Regents of the University of Wisconsin-Madison
 * 	- Glencoe Software, Inc.
 * 	- University of Dundee
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package io.scif.lifesci;

import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.img.axes.SCIFIOAxes;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.Interval;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleInputStream;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;
import org.scijava.util.Bytes;

/**
 * SDTReader is the file format reader for Becker &amp; Hickl SPC-Image SDT
 * files.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 */
@Plugin(type = Format.class)
public class SDTFormat extends AbstractFormat {

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "SPCImage Data";
	}

	@Override
	protected String[] makeSuffixArray() {
		return new String[] { "sdt" };
	}

	// -- Nested Classes --

	/**
	 *
	 */
	public static class Metadata extends AbstractMetadata {

		// -- Fields --

		/** Object containing SDT header information. */
		private SDTInfo info;

		/** Offset to binary data. */
		private int binOffset;

		/** Number of time bins in lifetime histogram. */
		private int timeBins;

		/** Number of spectral channels. */
		private int channels;

		/** Whether to combine lifetime bins into single intensity image planes. */
		private boolean mergeIntensity = false;

		private double timeBase;

		// -- SDT field getters/setters --

		public double getTimeBase() {
			return timeBase;
		}

		public void setTimeBase(double timeBase) {
			this.timeBase = timeBase;
		}

		public SDTInfo getSDTInfo() {
			return info;
		}

		public void setSDTInfo(final SDTInfo info) {
			this.info = info;
		}

		public int getBinOffset() {
			return binOffset;
		}

		public void setBinOffset(final int binOffset) {
			this.binOffset = binOffset;
		}

		public int getTimeBins() {
			return timeBins;
		}

		public void setTimeBins(final int timeBins) {
			this.timeBins = timeBins;
		}

		public int getChannels() {
			return channels;
		}

		public void setChannels(final int channels) {
			this.channels = channels;
		}

		public boolean mergeIntensity() {
			return mergeIntensity;
		}

		public void setMergeIntensity(final boolean mergeIntensity) {
			if (mergeIntensity != this.mergeIntensity) {
				this.mergeIntensity = mergeIntensity;
				// re-populate imageMetadata
				populateImageMetadata();
			}
		}

		// -- Metadat API Methods --

		@Override
		public void populateImageMetadata() {
			createImageMetadata(1);

			final ImageMetadata iMeta = get(0);
			if (!mergeIntensity()) {
				iMeta.addAxis(SCIFIOAxes.LIFETIME, getSDTInfo().timeBins);
				CalibratedAxis axis = iMeta.getAxis(SCIFIOAxes.LIFETIME);
				axis.setUnit("ns");
				double scale = getTimeBase() / getSDTInfo().timeBins;
				FormatTools.calibrate(iMeta.getAxis(SCIFIOAxes.LIFETIME), scale, 0.0);
				iMeta.setPlanarAxisCount(3);
			}
			iMeta.addAxis(Axes.X, getSDTInfo().width);
			iMeta.addAxis(Axes.Y, getSDTInfo().height);
			iMeta.addAxis(SCIFIOAxes.SPECTRA, channels);

			iMeta.setPixelType(FormatTools.UINT16);

			iMeta.setLittleEndian(true);
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
			iMeta.setMetadataComplete(true);
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			super.close(fileOnly);
			if (!fileOnly) {
				binOffset = timeBins = channels = 0;
				info = null;
			}
		}
	}

	/**
	 *
	 */
	public static class Parser extends AbstractParser<Metadata> {

		// -- Fields --

		/** Whether to combine lifetime bins into single intensity image planes. */
		private boolean mergeIntensity = false;

		// -- SDTParser API Methods --

		/**
		 * @return if true, this Reader is combining lifetime bins to a single
		 *         intensity plane.
		 */
		public boolean mergeIntensity() {
			return mergeIntensity;
		}

		/**
		 * @param mergeIntensity - Whether or not lifetime bins should be combined
		 *          to a single intensity plane.
		 */
		public void setMergeIntensity(final boolean mergeIntensity) {
			this.mergeIntensity = mergeIntensity;
		}

		// -- Parser API methods --

		@Override
		protected void typedParse(final DataHandle<Location> stream,
			final Metadata meta, final SCIFIOConfig config) throws IOException,
			FormatException
		{
			stream.setLittleEndian(true);

			log().debug("Reading SDT header");

			// read file header information
			final SDTInfo info = new SDTInfo(stream, meta.getTable());
			meta.setSDTInfo(info);
			meta.setBinOffset(info.dataBlockOffs + 22);
			meta.setTimeBins(info.timeBins);
			meta.setChannels(info.channels);

			meta.getTable().put("time bins", meta.getTimeBins());
			meta.getTable().put("channels", meta.getChannels());

			final double timeBase = 1e9 * info.tacR / info.tacG;
			meta.getTable().put("time base", timeBase);
			meta.timeBase = timeBase;
			meta.mergeIntensity = mergeIntensity;
		}
	}

	/**
	 *
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- AbstractReader Methods --

		@Override
		protected String[] createDomainArray() {
			return new String[] { FormatTools.FLIM_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final long planeIndex,
			final ByteArrayPlane plane, final Interval bounds,
			final SCIFIOConfig config) throws FormatException, IOException
		{
			final Metadata m = getMetadata();
			final byte[] buf = plane.getBytes();
			FormatTools.checkPlaneForReading(m, imageIndex, planeIndex, buf.length,
				bounds);

			final int sizeX = (int) m.get(imageIndex).getAxisLength(Axes.X);
			final int sizeY = (int) m.get(imageIndex).getAxisLength(Axes.Y);
			final int bpp = FormatTools.getBytesPerPixel(m.get(imageIndex)
				.getPixelType());
			// bytes per transient
			final int bpt = bpp * m.getTimeBins();
			final boolean little = m.get(imageIndex).isLittleEndian();

			final int paddedWidth = sizeX + ((4 - (sizeX % 4)) % 4);
			final int planeSize = paddedWidth * sizeY * bpt;

			final int x = (int) bounds.min(m.get(imageIndex).getAxisIndex(Axes.X)), //
					y = (int) bounds.min(m.get(imageIndex).getAxisIndex(Axes.Y)), //
					w = (int) bounds.dimension(m.get(imageIndex).getAxisIndex(Axes.X)), //
					h = (int) bounds.dimension(m.get(imageIndex).getAxisIndex(Axes.Y));

			final boolean merge = m.mergeIntensity();
			// Csarseven data has to read the entire image, so we may need to crop out
			// the undesired region
			byte[] b = !merge ? buf : new byte[sizeY * sizeX * bpt];

			final SDTInfo info = m.getSDTInfo();

			// FIFO support
			if (info.measMode == 13) {
				// Contains multiple data blocks. Each data block contains one or more
				// complete planes. Planes are assumed to be stored as they would be
				// for single block datasets.
				int tmpOff = info.dataBlockOffs;
				getHandle().seek(tmpOff);
				info.readBlockHeader(getHandle());
				// Compute channel + block indices from the requested plane index.
				final int channelIndex = (int) (planeIndex % info.noOfDataBlocks);
				final int blockIndex = (int) (planeIndex / info.noOfDataBlocks);
				// Seek to the data block for this plane index
				for (int i = 0; i < blockIndex; i++) {
					tmpOff = info.nextBlockOffs;
					getHandle().seek(tmpOff);
					info.readBlockHeader(getHandle());
				}
				// Skip to the requested plane and row offset
				getHandle().skip(channelIndex * planeSize + y * paddedWidth * bpp * m
					.getTimeBins());
			}
			// Csarseven support
			else if (info.noOfDataBlocks > 1) {
				int tmpOff = info.dataBlockOffs;
				final boolean crop = x == 0 && y == 0 && w == sizeX && y == sizeY;
				if (crop && !merge) {
					b = new byte[sizeY * sizeX * bpt];
				}
				// Data is stored by row, bottom row first
				for (int row = h - 1; row >= 0; row--) {
					for (int col = 0; col < w; col++) {

						getHandle().seek(tmpOff);
						info.readBlockHeader(getHandle());
						// Skip to the desired plane (channel)
						// Assuming channels are interleaved
						getHandle().skip(planeIndex * bpt);
						// Reads the current data block. Each data block contains all
						// the time bins for a single pixel position.
						getHandle().read(b, ((row * w) + col) * bpt, bpt);
						// Update offset to point to the next data block (pixel)
						tmpOff = info.nextBlockOffs;
					}
				}
				if (crop) {
					// We always have to read the entire plane since we go from pixel to
					// pixel, so now we can crop out what we didn't need.
					for (int row = 0; row < h; row++) {
						System.arraycopy(b, ((row * sizeX) + x) * bpt,
							buf, row * w * bpt, w * bpt);
					}
				}
			}
			// Standard offset
			else {
				// binOffset points to the start of the pixels, then we skip the
				// required number of planes and rows.
				getHandle().seek(m.getBinOffset() + planeIndex * planeSize + y *
					paddedWidth * bpt);
			}

			// For the SDT subtypes with complete planes per data block, we can read
			// the requested plane data now.
			if (info.measMode == 13 || info.noOfDataBlocks == 1) {
				final InputStream is;
				// compression detection
				// see https://www.lfd.uci.edu/~gohlke/code/sdtfile.py.html
				if ((info.blockType & 0x1000) != 0) {
					is = new ZipInputStream(new DataHandleInputStream<>(getHandle()));
					((ZipInputStream) is).getNextEntry();
				}
				else
					is = new DataHandleInputStream<>(getHandle());

				for (int row = 0; row < h; row++) {
					is.skip(bpt * x);
					readBytes(b, is, row * bpt * w, bpt * w);
					is.skip(bpt * (paddedWidth - x - w));
				}
				is.close();
			}

			// no pixel merging required
			if (!merge) {
				return plane;
			}

			for (int row = 0; row < h; row++) {
				final int yi = (y + row) * sizeX * bpt;
				final int ri = row * w * bpp;
				for (int col = 0; col < w; col++) {
					final int xi = yi + (x + col) * bpt;
					final int ci = ri + col * bpp;
					// combine all lifetime bins into single intensity value
					short sum = 0;
					for (int t = 0; t < m.getTimeBins(); t++) {
						sum += Bytes.toShort(b, xi + t * bpp, little);
					}
					Bytes.unpack(sum, buf, ci, 2, little);
				}
			}
			return plane;
		}

		/**
		 * Reads {@code nBytes} bytes from input stream {@code is} to {@code buf}
		 * starting at {@code offset}.
		 * 
		 * @param buf the destination
		 * @param is the source
		 * @param offset the offset in {@code buf} to start writing
		 * @param nBytes the number of bytes to read
		 * @throws IOException see {@link InputStream#read(byte[], int, int)}
		 */
		private static void readBytes(final byte[] buf, final InputStream is,
				final int offset, final int nBytes) throws IOException {
			int nRead = 0;
			while (nRead < nBytes) {
				int n = is.read(buf, offset + nRead, nBytes - nRead);
				if (n <= 0)
					break;
				nRead += n;
			}
		}
	}
}

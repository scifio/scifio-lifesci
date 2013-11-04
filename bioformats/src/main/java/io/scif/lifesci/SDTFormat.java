/*
 * #%L
 * SCIFIO Proprietary Formats
 * %%
 * Copyright (C) 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
import io.scif.img.axes.SCIFIOAxes;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;

import loci.common.DataTools;
import net.imglib2.meta.Axes;

import org.scijava.plugin.Plugin;

/**
 * SDTReader is the file format reader for Becker &amp; Hickl SPC-Image SDT
 * files.
 * 
 * @author Curtis Rueden ctrueden at wisc.edu
 */
@Plugin(type = Format.class)
public class SDTFormat extends AbstractFormat {

	// -- Format API Methods --

	@Override
	public String getFormatName() {
		return "SPCImage Data";
	}

	@Override
	public String[] getSuffixes() {
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

		// -- SDT field getters/setters --

		public SDTInfo getSDTInfo() {
			return info;
		}

		public void setSDTInfo(SDTInfo info) {
			this.info = info;
		}

		public int getBinOffset() {
			return binOffset;
		}

		public void setBinOffset(int binOffset) {
			this.binOffset = binOffset;
		}

		public int getTimeBins() {
			return timeBins;
		}

		public void setTimeBins(int timeBins) {
			this.timeBins = timeBins;
		}

		public int getChannels() {
			return channels;
		}

		public void setChannels(int channels) {
			this.channels = channels;
		}

		public boolean mergeIntensity() {
			return mergeIntensity;
		}

		public void setMergeIntensity(boolean mergeIntensity) {
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

			ImageMetadata iMeta = get(0);

			if (!mergeIntensity()) {
				iMeta.addAxis(SCIFIOAxes.LIFETIME, getSDTInfo().timeBins);
				iMeta.setPlanarAxisCount(3);
			}

			iMeta.addAxis(Axes.X, getSDTInfo().width);
			iMeta.addAxis(Axes.Y, getSDTInfo().height);
			iMeta.addAxis(Axes.CHANNEL, channels);

			iMeta.setPixelType(FormatTools.UINT16);

			if (mergeIntensity() && iMeta.getAxisLength(Axes.CHANNEL) > 1) {
				iMeta.setPlanarAxisCount(iMeta.getPlanarAxisCount() + 1);
			}

			iMeta.setLittleEndian(true);
			iMeta.setIndexed(false);
			iMeta.setFalseColor(false);
			iMeta.setMetadataComplete(true);
		}

		@Override
		public void close(boolean fileOnly) throws IOException {
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
		public void setMergeIntensity(boolean mergeIntensity) {
			this.mergeIntensity = mergeIntensity;
		}

		// -- Parser API methods --

		@Override
		protected void typedParse(RandomAccessInputStream stream, Metadata meta)
			throws IOException, FormatException
		{
			stream.order(true);

			log().info("Reading SDT header");

			// read file header information
			SDTInfo info = new SDTInfo(stream, meta.getTable());
			meta.setSDTInfo(info);
			meta.setBinOffset(info.dataBlockOffs + 22);
			meta.setTimeBins(info.timeBins);
			meta.setChannels(info.channels);

			meta.getTable().put("time bins", meta.getTimeBins());
			meta.getTable().put("channels", meta.getChannels());
			meta.getTable().put("time base", 1e9 * info.tacR / info.tacG);
			meta.mergeIntensity = mergeIntensity;
		}
	}

	/**
	 *
	 */
	public static class Reader extends ByteArrayReader<Metadata> {

		// -- Constructor --

		public Reader() {
			domains = new String[] { FormatTools.FLIM_DOMAIN };
		}

		// -- Reader API Methods --

		@Override
		public ByteArrayPlane openPlane(int imageIndex, long planeIndex,
			ByteArrayPlane plane, long[] planeMin, long[] planeMax)
			throws FormatException, IOException
		{
			Metadata m = getMetadata();
			byte[] buf = plane.getBytes();
			FormatTools.checkPlaneForReading(m, imageIndex, planeIndex, buf.length,
				planeMin, planeMax);

			int sizeX = (int) m.get(imageIndex).getAxisLength(Axes.X);
			int sizeY = (int) m.get(imageIndex).getAxisLength(Axes.Y);
			int bpp = FormatTools.getBytesPerPixel(m.get(imageIndex).getPixelType());
			boolean little = m.get(imageIndex).isLittleEndian();

			// HACK
			// adjust the number of time bins if DimensionSwapper was used
			if (buf.length == sizeY * sizeX * bpp && m.getTimeBins() > 1) {
				m.setTimeBins(1);
			}

			int paddedWidth = sizeX + ((4 - (sizeX % 4)) % 4);
			int planeSize = paddedWidth * sizeY * m.getTimeBins() * bpp;

			int x = (int) planeMin[m.get(imageIndex).getAxisIndex(Axes.X)], y =
				(int) planeMin[m.get(imageIndex).getAxisIndex(Axes.Y)], w =
				(int) planeMax[m.get(imageIndex).getAxisIndex(Axes.X)], h =
				(int) planeMax[m.get(imageIndex).getAxisIndex(Axes.Y)];

			boolean direct = !m.mergeIntensity();
			byte[] b = direct ? buf : new byte[sizeY * sizeX * m.getTimeBins() * bpp];
			getStream().seek(
				m.getBinOffset() + planeIndex * planeSize + y * paddedWidth * bpp *
					m.getTimeBins());

			for (int row = 0; row < h; row++) {
				getStream().skipBytes(x * bpp * m.getTimeBins());
				getStream().read(b, row * bpp * m.getTimeBins() * w,
					w * m.getTimeBins() * bpp);
				getStream().skipBytes(bpp * m.getTimeBins() * (paddedWidth - x - w));
			}

			if (direct) return plane; // no cropping required

			int scan = m.mergeIntensity() ? bpp : m.getTimeBins() * bpp;

			for (int row = 0; row < h; row++) {
				int yi = (y + row) * sizeX * m.getTimeBins() * bpp;
				int ri = row * w * scan;
				for (int col = 0; col < w; col++) {
					int xi = yi + (x + col) * m.getTimeBins() * bpp;
					int ci = ri + col * scan;
					if (m.mergeIntensity()) {
						// combine all lifetime bins into single intensity value
						short sum = 0;
						for (int t = 0; t < m.getTimeBins(); t++) {
							sum += DataTools.bytesToShort(b, xi + t * bpp, little);
						}
						DataTools.unpackBytes(sum, buf, ci, 2, little);
					}
					else {
						// each lifetime bin is a separate interleaved "channel"
						System.arraycopy(b, xi, buf, ci, scan);
					}
				}
			}
			return plane;
		}

	}
}

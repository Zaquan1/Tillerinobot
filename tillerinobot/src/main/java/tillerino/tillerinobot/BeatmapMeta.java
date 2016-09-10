package tillerino.tillerinobot;

import java.text.DecimalFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BitwiseMods;

import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.PercentageEstimates;

@Data
@AllArgsConstructor
public class BeatmapMeta {
	OsuApiBeatmap beatmap;

	Integer personalPP;
	
	PercentageEstimates estimates;

	static DecimalFormat format = new DecimalFormat("#.##");

	static DecimalFormat noDecimalsFormat = new DecimalFormat("#");

	public String formInfoMessage(boolean formLink, String addition, int hearts, Double acc, Integer combo, Integer misses) {
		
		String beatmapName = getBeatmap().getArtist() + " - " + getBeatmap().getTitle()
				+ " [" + getBeatmap().getVersion() + "]";
		if(formLink) {
			beatmapName = "[http://osu.ppy.sh/b/" + getBeatmap().getBeatmapId() + " " + beatmapName + "]";
		}
		
		PercentageEstimates percentageEstimates = getEstimates();
		long mods = percentageEstimates.getMods();
		if (percentageEstimates.getMods() != 0) {
			StringBuilder modsString = new StringBuilder();
			for (Mods mod : Mods.getMods(percentageEstimates.getMods())) {
				if (mod.isEffective()) {
					modsString.append(mod.getShortName());
				}
			}
			beatmapName += " " + modsString;
		}

		String estimateMessage = "";
		if(getPersonalPP() != null) {
			estimateMessage += "future you: " + getPersonalPP() + "pp | ";
		}
		

		if (acc != null) {
			estimateMessage += format.format(acc * 100) + "%";
			if(combo != null && misses != null) {
				estimateMessage += " " + combo + "x " + misses + "miss: ";
				estimateMessage += ": " + noDecimalsFormat.format(percentageEstimates.getPP(acc, combo, misses)) + "pp";
			} else {
				estimateMessage += ": " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(acc)) + "pp";
			}
		} else {
			estimateMessage += "95%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.95)) + "pp";
			estimateMessage += " | 98%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.98)) + "pp";
			estimateMessage += " | 99%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(.99)) + "pp";
			estimateMessage += " | 100%: " + noDecimalsFormat.format(percentageEstimates.getPPForAcc(1)) + "pp";
		}
		if (percentageEstimates.isShaky()) {
			estimateMessage += " (rough estimates)";
		}
		
		estimateMessage += " | " + secondsToMinuteColonSecond(getBeatmap().getTotalLength(mods));

		Double starDiff = null;
		if (mods == 0) {
			starDiff = beatmap.getStarDifficulty();
		} else {
			starDiff = estimates.getStarDiff();
		}
		if (starDiff != null) {
			estimateMessage += " ★ " + format.format(starDiff);
		}

		estimateMessage += " ♫ " + format.format(getBeatmap().getBpm(mods));
		estimateMessage += " AR" + format.format(getBeatmap().getApproachRate(mods));
		estimateMessage += " OD" + format.format(getBeatmap().getOverallDifficulty(mods));
		
		if (estimates.isOppaiOnly()) {
			estimateMessage += " (";
			if (!estimates.isRanked()) {
				estimateMessage += "unranked; ";
			}
			estimateMessage += "all [https://github.com/Francesco149/oppai oppai])";
		}

		String heartString = hearts > 0 ? " " + StringUtils.repeat('♥', hearts) : "";

		return beatmapName + "   " + estimateMessage + (addition != null ? "   " + addition : "") + heartString;
	}

	public static String secondsToMinuteColonSecond(int length) {
		return length / 60 + ":" + StringUtils.leftPad(String.valueOf(length % 60), 2, '0');
	}
	
	/**
	 * Mods presented by this meta
	 * @return 0 if estimates are old style
	 */
	@BitwiseMods
	public long getMods() {
		return estimates.getMods();
	}
	
	public BeatmapWithMods getBeatmapWithMods() {
		return new BeatmapWithMods(beatmap.getBeatmapId(), estimates.getMods());
	}
}

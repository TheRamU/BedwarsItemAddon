package me.ram.bedwarsitemaddon.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import lombok.Getter;
import me.ram.bedwarsitemaddon.Main;

public class LocaleConfig {

	@Getter
	private EnumLocale pluginLocale;
	private final Map<String, Object> language;

	public LocaleConfig() {
		language = new HashMap<String, Object>();
	}

	private void loadLanguage() {
		switch (pluginLocale) {
		case ZH_CN:
			language.put("version", "版本");
			language.put("author", "作者");
			language.put("update_checking", "§b§lBWIA §f>> §a正在检测更新...");
			language.put("no_update", "§b§lBWIA §f>> §a您使用的已是最新版本！");
			language.put("update_check_failed", "§b§lBWIA §f>> §c检测更新失败，请检查服务器网络连接！");
			language.put("update_info", "检测到版本更新！");
			language.put("running_version", "当前版本");
			language.put("update_version", "更新版本");
			language.put("updates", "更新内容");
			language.put("update_download", "更新地址");
			break;
		case EN_US:
			language.put("version", "Version");
			language.put("author", "Author");
			language.put("update_checking", "§b§lBWIA §f>> §aUpdate check...");
			language.put("no_update", "§b§lBWIA §f>> §aYou are running the latest version!");
			language.put("update_check_failed", "§b§lBWIA §f>> §cUpdate check failed! Please check the server network!");
			language.put("update_info", "There are version update!");
			language.put("running_version", "Running version");
			language.put("update_version", "Update version");
			language.put("updates", "Updates");
			language.put("update_download", "Download");
			break;
		case ZH_TW:
			language.put("version", "版本");
			language.put("author", "作者");
			language.put("update_checking", "§b§lBWIA §f>> §a正在檢測更新...");
			language.put("no_update", "§b§lBWIA §f>> §a您使用的已是最新版本！");
			language.put("update_check_failed", "§b§lBWIA §f>> §c檢測更新失敗，請檢查服務器網絡連接！");
			language.put("update_info", "檢測到版本更新！");
			language.put("running_version", "當前版本");
			language.put("update_version", "更新版本");
			language.put("updates", "更新內容");
			language.put("update_download", "更新地址");
			break;
		default:
			break;
		}
	}

	public void loadLocaleConfig() {
		File folder = new File(Main.getInstance().getDataFolder(), "/");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File file = new File(folder.getAbsolutePath() + "/config.yml");
		if (file.exists()) {
			pluginLocale = getLocaleByName(YamlConfiguration.loadConfiguration(file).getString("locale", "en_US"));
		} else {
			pluginLocale = getSystemLocale();
		}
		loadLanguage();
		saveLocale();
	}

	public Object getLanguage(String str) {
		return language.getOrDefault(str, "null");
	}

	public String getSystemLocaleName() {
		Locale locale = Locale.getDefault();
		return locale.getLanguage() + "_" + locale.getCountry();
	}

	public EnumLocale getSystemLocale() {
		return getLocaleByName(getSystemLocaleName());
	}

	private EnumLocale getLocaleByName(String name) {
		EnumLocale locale = EnumLocale.getByName(name);
		return locale == null ? EnumLocale.EN_US : locale;
	}

	public void saveResource(String resourcePath) {
		try {
			writeToLocal(Main.getInstance().getDataFolder().getPath() + "/" + resourcePath, Main.getInstance().getResource("locale/" + getPluginLocale().getName() + "/" + resourcePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void saveLocale() {
		File folder = new File(Main.getInstance().getDataFolder(), "/locale");
		if (!folder.exists()) {
			folder.mkdirs();
			for (EnumLocale locale : EnumLocale.values()) {
				File locale_folder = new File(folder.getPath(), "/" + locale.getName());
				if (!locale_folder.exists()) {
					locale_folder.mkdirs();
				}
				for (String file : new String[] { "config.yml" }) {
					try {
						writeToLocal(folder.getPath() + "/" + locale.getName() + "/" + file, Main.getInstance().getResource("locale/" + locale.getName() + "/" + file));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static void writeToLocal(String destination, InputStream input) throws IOException {
		int index;
		byte[] bytes = new byte[1024];
		FileOutputStream downloadFile = new FileOutputStream(destination);
		while ((index = input.read(bytes)) != -1) {
			downloadFile.write(bytes, 0, index);
			downloadFile.flush();
		}
		downloadFile.close();
		input.close();
	}
}

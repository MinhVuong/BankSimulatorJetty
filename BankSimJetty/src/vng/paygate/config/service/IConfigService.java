package vng.paygate.config.service;

import vng.paygate.domain.bo.BoModuleConfig;

/**
 *
 * @author VuongTM
 */
public interface IConfigService<T extends BoModuleConfig> {

    public abstract T getModuleConfig();

    public abstract void refreshModuleConfig();

//    public abstract void loadDatabaseConfigs();
}

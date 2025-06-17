package com.github.hbq969.code.zkc.service.impl;

import com.github.hbq969.code.common.initial.ScriptInitialAware;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.spring.i18n.LangInfo;
import com.github.hbq969.code.common.spring.i18n.LanguageEvent;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.code.common.utils.InitScriptUtils;
import com.github.hbq969.code.common.utils.StrUtils;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.code.sm.login.service.LoginService;
import com.github.hbq969.code.zkc.config.ZkcProperties;
import com.github.hbq969.code.zkc.service.InitialService;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author : hbq969@gmail.com
 * @description : 初始化业务类
 * @createTime : 2025/1/2 10:35
 */
@Service("zkc-InitialServiceImpl")
@Slf4j
public class InitialServiceImpl implements InitialService, ScriptInitialAware {

    @Autowired
    private SpringContext context;

    @Autowired
    private ZkcProperties conf;

    @Autowired
    private LoginService loginService;

    @Override
    public String nameOfScriptInitialAware() {
        return "h-zkc-initial-script";
    }

    @Override
    public void tableCreate() {
        String profile = StringUtils.equals("oracle", conf.getDialect()) ? "oracle" : StringUtils.equals("embedded", conf.getDialect()) ? null : "mysql";
        InitScriptUtils.initial(context, "/", "zkc-schema.sql", profile, StandardCharsets.UTF_8, null);
    }

    @Override
    public void scriptInitial() {
        String lang = com.github.hbq969.code.sm.login.utils.I18nUtils.getFullLanguage(context);
        String filename = String.join("", "zkc-initial", "-", lang, ".sql");
        Map map = ImmutableMap.of("menuPrefixPath", context.getProperty("zkc.menu.prefix-path", ""));
        com.github.hbq969.code.common.utils.InitScriptUtils.initial(context, filename, StandardCharsets.UTF_8, (sql) -> StrUtils.replacePlaceHolders(sql, map, ""), () -> {
            loginService.loadSMInfo();
            context.getOptionalBean(MapDictHelperImpl.class).ifPresent(dict -> dict.reloadImmediately());
        });
    }

    @Override
    public void onApplicationEvent(LanguageEvent event) {
        LangInfo langInfo = (LangInfo) event.getSource();
        if (log.isDebugEnabled()) {
            log.debug("h-zkc 监听到语言变化事件: {}", GsonUtils.toJson(langInfo));
        }
        scriptInitial();
    }

    @Override
    public int orderOfScriptInitialAware() {
        return ScriptInitialAware.super.orderOfScriptInitialAware();
    }
}

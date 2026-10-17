package cn.xiaofuge.x.domain.form.adapter.port;

import cn.xiaofuge.x.domain.form.model.FormDefinition;

public interface IFormDefinitionCache {
    FormDefinition get(String shareCode);

    void put(FormDefinition form);

    void evict(String shareCode);
}

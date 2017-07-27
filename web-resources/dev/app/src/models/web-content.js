import ValamisModel from 'models/valamis';
import appConfig from 'config';

import LiferayUtil from 'utils/liferay-utils';

const WebContentModel = ValamisModel.extend({
    getUrl() {
        let url = appConfig().endpoint.actionUrl
            + '&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view'
            + '&p_p_col_count=1&p_p_col_id=column-1&p_p_cacheability=cacheLevelPage'
            + '&p_p_state=normal'
            + '&articleID=' + this.get('id')
            + '&language=' + LiferayUtil.getUserLanguageId()
            + '&resourceType=web-content';

        return url;
    }
});

export default WebContentModel
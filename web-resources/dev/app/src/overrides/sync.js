import _ from "lodash";
import LiferayUtil from 'utils/liferay-utils';

export default class SyncOverride {
    static init() {
        const oldBackboneSync = Backbone.sync;
        Backbone.sync = function (method, model, options) {

            let csrf = LiferayUtil.authToken();

            let layoutId = LiferayUtil.plId();
            let headers = {
                'layoutId': layoutId,
                'X-VALAMIS-Layout-Id': layoutId,
                'X-VALAMIS-Course-Id': LiferayUtil.courseId()
            };

            if (!options.data && method !== 'read') {
                options.contentType = 'application/json';
                options.data = JSON.stringify(model.toJSON());
            }

            if (method != 'read') {
                _.extend(headers, {'X-CSRF-Token': csrf});
            }

            options.beforeSend = function (xhr) {
                _.each(headers, (v, k) => {
                    xhr.setRequestHeader(k, v);
                })
            };

            return new Promise((resolve, reject) => {
                oldBackboneSync
                    .apply(this, [method, model, options])
                    .then(
                        (...params) => {
                            resolve(...params);
                        },
                        (...params) => {
                            reject(...params);
                        }
                    );
            });
        };
    }
}
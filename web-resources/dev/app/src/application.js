import { mixin } from 'core-decorators';
import i18nLoader from 'i18n/reader';
import LiferayUtil from 'utils/liferay-utils';

import RendererOverride from 'overrides/render'
import SyncOverride from 'overrides/sync'
import NotifyService from 'notify/notify-service'
import ModalService from 'modals/modal-service'
import appConfig from 'config';

import Radio from 'backbone.radio';
import LiferayActivities from 'collections/liferay-activities';

import RootLayout from 'views/list/layout';

class ValamisApplication extends Marionette.Application {
    start(options) {
        // import 'babel-polyfill';
        // only one instance of babel-polyfill is allowed
        if (!global._babelPolyfill) {
            require('babel-polyfill');
        }

        // todo not the best way, will be fixed when will use modules
        this.language = options.language;
        this.permissions = options.permissions;
        RendererOverride.init({
            language: options.language,
            permissions: options.permissions
        });

        SyncOverride.init();
        super.start();
    }
}

@mixin({
    region: '#learningPathsAppRegion'
})
class App extends ValamisApplication {

    start(options, test) {
        let langPath = (lang) => {
            return `${appConfig().resourcePath}` + 'i18n/' + 'learningPaths_' + lang + '.properties';
        };
        let lang = LiferayUtil.getUserLocale();
        let defaultLocale = 'en';

        let url = langPath(lang);
        let defaultUrl = langPath(defaultLocale);

        i18nLoader.loadProperties(url, defaultUrl,
            (data) => {
                "use strict";
                super.start({
                    language: data,
                    permissions: appConfig().permissions
                });
            },
            (error) => {
                "use strict";
                console.error("Can't load locale resources (user or default)");
            }
        );
    }

    onStart() {
        //TODO we can use liferay events to clean up everything after portlet unload
        // for compatibility with lr7
        // reset region when start application to delete the cached el
        this.getRegion().reset();

        // destroy services if they have been already created (at prev app start)
        if (this.notifyService && _.isFunction(this.notifyService.destroy)) {
            this.notifyService.destroy();
        }
        if (this.modalService && _.isFunction(this.modalService.destroy)) {
            this.modalService.destroy();
        }

        if (window.hasOwnProperty('elementQuery')) {
            elementQuery.init();
        }
        // end of compatibility stuff

        const rootLayout = new RootLayout();

        this.notifyService = new NotifyService();
        this.modalService = new ModalService({region: rootLayout.getRegion('modals')});

        let that = this;
        this.openLpId = undefined;

        // router for opening learning path by link
        let Router = Backbone.Router.extend({
            routes: {
                '': 'index',
                'learning-path/:id': 'openedByLink'
            },
            index: function () {},
            openedByLink: function (id) {
                that.openLpId = id;
            }
        });

        this.router = new Router();

        if (!Backbone.History.started) {
            Backbone.history.start();
        }

        this.activities = new LiferayActivities();
        this.activities.fetch().then(
            () => { this.showView(rootLayout); },
            () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
        );
    }
}

export default new App();
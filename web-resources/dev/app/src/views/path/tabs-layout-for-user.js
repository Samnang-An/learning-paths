
import template from './tabs-layout.html';

import PathPreview from './preview/layout';
import PathViewer from './viewer/layout';

const TabsLayoutForUser  = Marionette.View.extend({
    template: template,
    regions: {
        info: '#pathInfo',
        layout: '#extLayout'
    },
    ui: {
        pathTabs: '.js-valamis-tabs',
    },
    childViewEvents: {
        'viewer:back': 'backFromViewer',
    },
    modelEvents: {
        'open:viewer': 'showViewer',
        'user:joined': 'fetchAndOpenPreview',
        'user:left': 'showPreview',
    },
    onRender() {
        this.showPreview();
        this.ui.pathTabs.valamisTabs();
    },

    // preview mode functions
    showPreview(reloadModel) {
        this.showChildView('info', new PathPreview({
            model: this.model,
            reloadModel: reloadModel
        }));
    },
    fetchAndOpenPreview() {
        this.showPreview(true);
    },

    // viewer
    showViewer(goals) {
        this.ui.pathTabs.addClass('hidden');

        this.showChildView('layout', new PathViewer({
            model: this.model,
            goals: goals
        }));
    },
    backFromViewer() {
        this.showPreview(true);
        this.ui.pathTabs.removeClass('hidden');
        this.getRegion('layout').empty();
    }

});

export default TabsLayoutForUser;
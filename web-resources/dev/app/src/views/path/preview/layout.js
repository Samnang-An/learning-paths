import template from './layout.html';

import App from 'application';
import Radio from 'backbone.radio';

import ValamisUIControls from 'behaviors/valamis-ui-controls';

import PathInfoPreview from './info';
import LearningPathWithUserData from 'models/learning-path-for-user';

const PathPreview  = Marionette.View.extend({
    template: template,
    regions: {
        info: '.js-preview-info'
    },
    ui: {
        edit: '.js-edit',
        clone: '.js-clone-path',
        publish: '.js-publish-path',
        activate: '.js-activate-path',
        deactivate: '.js-deactivate-path',
        delete: '.js-delete-path',
    },
    events: {
        'click @ui.edit': 'editPath',
        'click @ui.clone': 'clonePath',
        'click @ui.publish': 'publishPath',
        'click @ui.activate': 'activatePath',
        'click @ui.deactivate': 'deactivatePath',
        'click @ui.delete': 'confirmDeletePath',
    },
    behaviors: [ValamisUIControls],
    initialize() {
        this.notifyChannel = Radio.channel('notify');
        this.reloadModel = this.options.reloadModel;
    },
    onRender() {
        if (this.reloadModel) {
            // add small delay because checker works at the background
            _.delay(() => {
                this.fetchNewData();
            }, 500);
        }
        else {
            this.showInfo();
        }
    },
    fetchNewData() {
        let pathWithUserData = new LearningPathWithUserData({id: this.model.get('id')});

        pathWithUserData.fetch().then(
            () => {
                this.model.clear({ silent: true });
                this.model.set(pathWithUserData.toJSON());
                this.reloadModel = false;
                this.render();
            },
            () => {
                Radio.channel('notify').trigger('notify', 'error', 'failedLabel');
            }
        );
    },
    showInfo() {
        this.showChildView('info', new PathInfoPreview({
            model: this.model,
            doAsAdmin: this.options.doAsAdmin
        }));
    },
    // admin actions
    editPath() {
        this.triggerMethod('edit:path');
    },
    clonePath() {
        this.model.clone().then(
            () => { this.notifyChannel.trigger('notify', 'success', 'completeLabel'); },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    publishPath() {
        this.model.publish().then(
            () => {
                this.reloadModel = true;
                this.render();
            },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    activatePath() {
        this.model.activate().then(
            () => {
                this.model.set('activated', true);
                this.render();
            },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    deactivatePath() {
        this.model.deactivate().then(
            () => {
                this.model.set('activated', false);
                this.render();
            },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    confirmDeletePath() {
        this.notifyChannel.trigger(
            'confirm',
            { message: App.language['warningDeletePathLabel'] },
            () => { this.deletePath(); }
        );
    },
    deletePath() {
        this.model.destroy().then(
            () => { this.notifyChannel.trigger('notify', 'success', 'completeLabel'); },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    }
});

export default PathPreview;
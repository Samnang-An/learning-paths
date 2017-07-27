import template from './list-item.html';

import ValamisUIControls from 'behaviors/valamis-ui-controls';
import TilesResize from 'behaviors/tile-resize';

import * as enumerations from 'models/enumerations';
import Radio from 'backbone.radio';
import App from 'application';

const PathItemView  = Marionette.View.extend({
    template: template,
    templateContext() {
        let statuses = enumerations.statuses;
        let status = this.model.isMember() ? this.model.getStatus() : '';

        return {
            isMember: this.model.isMember(),
            isInProgress: status === statuses.inprogress,
            isSucceed: status === statuses.success,
            isFailed: status === statuses.failed,
            isExpired: status === statuses.expired,
            isExpiring: status === statuses.expiring,
            fullLogoUrl: this.model.getFullLogoUrl(),
            doAsAdmin: this.model.collection.doAsAdmin
        }
    },
    behaviors: [ValamisUIControls],
    modelEvents: {
        'change': 'render'
    },
    className() {
        return 'tile s-12 m-4 l-2';
    },
    ui: {
        'open': '.js-open-path',
        'join': '.js-join-path',
        'leave': '.js-leave-path',
        'clone': '.js-clone-path',
        'publish': '.js-publish-path',
        'activate': '.js-activate-path',
        'deactivate': '.js-deactivate-path',
        'delete': '.js-delete-path',
        'export': '.js-export-path'
    },
    events: {
        'click @ui.open': 'openPath',
        'click @ui.join': 'joinPath',
        'click @ui.leave': 'leavePath',
        'click @ui.clone': 'clonePath',
        'click @ui.publish': 'publishPath',
        'click @ui.activate': 'activatePath',
        'click @ui.deactivate': 'deactivatePath',
        'click @ui.delete': 'confirmDeletePath',
        'click @ui.export': 'exportPath'
    },
    onRender() {
        this.notifyChannel = Radio.channel('notify');
        this.$el.toggleClass('unpublished', !this.model.get('activated'));
    },
    openPath() {
        this.triggerMethod('open:modal', this.model);
    },
    joinPath() {
        let collection = this.model.collection;
        this.model.join().then(
            () => {
                if (this.model.collection.doAsAdmin) {
                    this.model.set('status', enumerations.statuses.inprogress);
                }
                else {
                    // remove only view not model
                    this.remove();
                }
                collection.trigger('reload:paths');
            },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    leavePath() {
        this.model.leave().then(
            () => {
                if (this.model.collection.doAsAdmin) {
                    this.model.unset('status');
                }
                else {
                    // remove only view not model
                    this.remove();
                }
            },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    clonePath() {
        this.model.clone().then(
            () => {
                this.notifyChannel.trigger('notify', 'success', 'completeLabel');
                this.model.trigger('reload:paths');
            },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    publishPath() {
        this.model.publish().then(
            () => { this.model.set({'published': true, 'activated': true, 'hasDraft': false}); },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    activatePath() {
        this.model.activate().then(
            () => { this.model.set('activated', true); },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    deactivatePath() {
        this.model.deactivate().then(
            () => { this.model.set('activated', false); },
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
        let collection = this.model.collection;
        this.model.destroy().then(
            () => {
                this.notifyChannel.trigger('notify', 'success', 'completeLabel');
                collection.trigger('reload:paths');
            },
            () => { this.notifyChannel.trigger('notify', 'error', 'failedLabel'); }
        );
    },
    exportPath() {
        // todo add export
    }
});

const PathsListView = Marionette.CollectionView.extend({
    childView: PathItemView,
    className() {
        return 'val-row ' + this.options.displayMode;
    },
    behaviors: [TilesResize],
    toggleDisplayMode(mode) {
        this.triggerMethod('display:mode:changed', mode);
    }
});

export default PathsListView;

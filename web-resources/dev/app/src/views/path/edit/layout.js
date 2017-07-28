
import template from './layout.html';

import PathInfoEdit from './info';
import LearningPathDraft from 'models/learning-path-draft';

import Radio from 'backbone.radio';

const PathEdit  = Marionette.View.extend({
    template: template,
    regions: {
        info: '.js-edit-info'
    },
    ui: {
        'publishButton': '.js-publish',
        'savingLabel': '.js-saving',
        'savedLabel': '.js-saved'
    },
    events: {
        'click @ui.publishButton': 'publishPath'
    },
    childViewEvents: {
        'select:goals:step1': 'onSelectGoals'
    },
    initialize() {
        this.model = new LearningPathDraft();
        this.pathModel = this.options.pathModel;
    },
    modelEvents: {
        request: 'showSavingLabel',
        sync: 'showSavedLabel'
    },
    onRender() {

        if (!this.pathModel.isNew()) {
            this.model.set({
                'id': this.pathModel.get('id'),
                'userMembersCount': this.pathModel.get('userMembersCount')
            });

            if (!this.pathModel.get('hasDraft')) {
                this.pathModel.createDraft().then(
                    (response) => {
                        this.pathModel.set('hasDraft', true);
                        this.model.set(response);
                        this.showInfo();
                    },
                    () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
                );
            }
            else {
                this.model.fetch().then(
                    () => { this.showInfo(); },
                    () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
                );
            }
        }
        else {
            this.ui.publishButton.prop('disabled', true);
            this.model.on('sync', this.showForSavedPath, this);

            this.showInfo();
        }
    },
    showSavingLabel() {
        this.toggleSaveLabels(false);
    },
    showSavedLabel() {
        this.toggleSaveLabels(true);
    },
    toggleSaveLabels(isFinished) {
        this.ui.savingLabel.toggleClass('hidden', isFinished);
        this.ui.savedLabel.toggleClass('hidden', !isFinished);
    },
    showForSavedPath() {
        this.triggerMethod('new:path:saved', this.model.toJSON());
        this.ui.publishButton.prop('disabled', false);
        this.model.off('sync', this.showForSavedPath);
    },
    showInfo() {
        let info = new PathInfoEdit({model: this.model});
        this.showChildView('info', info);
    },
    publishPath() {
        this.model.publish().then(
            () => {
                Radio.channel('notify').trigger('notify', 'success', 'completeLabel');
                this.openPreview();
            },
            () => { Radio.channel('notify').trigger('notify', 'error', 'failedLabel'); }
        );
    },
    openPreview() {
        this.triggerMethod('preview:path');
    },
    onSelectGoals(goalType) {
        this.triggerMethod('select:goals:step2', goalType, this.model);
    }
});

export default PathEdit;
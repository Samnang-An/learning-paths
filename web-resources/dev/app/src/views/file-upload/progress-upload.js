import template from "./progress-upload.html";

const ProgressUploadView = Marionette.View.extend({
    template: template,
    initialize: function () {
        this.listenTo(this.model, 'change:progress', this.updateUploadProgress);
    },
    updateUploadProgress: function () {
        var progressPercents = this.model.get('progress') + '%';
        this.$('.progress-bar')
            .css('width', progressPercents)
            .html(progressPercents);
    },
    hideInfo: function () {
        this.$('.info-block').hide();
        this.$('.progress-bar').html('');
    }
});

export default ProgressUploadView;
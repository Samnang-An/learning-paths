
import template from './confirmation-view.html';

const ConfirmationView  = Marionette.View.extend({
    template: template,
    ui: {
        confirm: '.js-confirmation',
        decline: '.js-decline'
    },
    events: {
        'click @ui.confirm': 'confirm',
        'click @ui.decline': 'decline'
    },
    templateContext: function() {
        return {
            message: this.options.message || '',
            showDontSaveButton: !!this.options.showDontSaveButton
        }
    },
    confirm: function () {
        this.trigger('confirmed', this);
    },
    decline: function () {
        this.trigger('declined', this);
    },
    onRender: function() {
        return this.$el;
    }
});

export default ConfirmationView;

import App from 'application';
import toastr from 'toastr';
import ConfirmationView from './confirmation-view';

// do not remove even it is shown as unused
import Radio from 'backbone.radio';

const NotifyService  = Marionette.Object.extend ({

    channelName: 'notify',
    radioEvents: {
        'notify': 'notify',
        'confirm': 'confirm'
    },

    notify: function(notificationType, messageLabel, options, title) {
        function getToastrFunc(type) {
            switch(type) {
                case 'success':
                    return toastr.success;
                    break;
                case 'warning':
                    return toastr.warning;
                    break;
                case 'error':
                    return toastr.error;
                    break;
                case 'clear':
                    return toastr.clear;
                    break;
                case 'info':
                default:
                    return toastr.info;
                    break;
            }
        }

        var toastrFunc = getToastrFunc(notificationType);
        options = options || {};
        _.extend(options, {'positionClass': 'toast-top-right'});
        if(!toastr.options.positionClass && !(options && options.positionClass)) {
            _.extend(options, {'positionClass': 'toast-top-right'});
        }
        if($('#toast-container').children().length > 0) {
            toastr.options.hideDuration = 0;
            toastr.clear();
            toastr.options.hideDuration = 1000;
        }
        var message = App.language[messageLabel] || messageLabel;
        toastrFunc(message, title, options);
    },

    confirm: function(options, onConfirm, onDecline) {
        var dialog = new ConfirmationView(options);
        dialog.on('confirmed',function() {
            dialog.destroy();
            if(_.isFunction(onConfirm)) {
                onConfirm();
            }
        });
        dialog.on('declined',function() {
            dialog.destroy();
            if(_.isFunction(onDecline)) {
                onDecline();
            }
        });

        var title = App.language[this.options.title] || this.options.title;
        toastr.info(dialog.render().$el, title, {
            'positionClass': 'toast-center',
            'timeOut': '0',
            'showDuration': '0',
            'hideDuration': '0',
            'extendedTimeOut': '0'
        });

    }

});

export default NotifyService;
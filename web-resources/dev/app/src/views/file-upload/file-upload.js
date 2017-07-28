/**
 * File uploader, based on https://github.com/blueimp/jQuery-File-Upload (MIT License)
 *
 * @constructor
 * @param {{endpoint: String}} options
 *    endpoint - where to upload files
 * @fires itemDone on file uploading complete
 *
 * @event itemDone
 * @param {Object} result - response from server
 * @param {ProgressUploadView} view - view of file item uploading process
 */

import ProgressUploadView from "./progress-upload";
import template from "./file-upload.html";
import appConfig from "config";

const FileUploadView = Marionette.View.extend({
    template: template,
    templateContext: function () {
        return {
            fileAttribute: this.getOption('fileAttribute'),
            fileUploadAdditionalInfo: this.getOption('message') || "",
            fileTypeAttribute: this.getOption('acceptedFileTypes')
        }
    },
    initialize: function (options) {
        options = options || {};
        this.progressView = ProgressUploadView;
        this.amount = 0;
        this.autoUpload = options.autoUpload;
        if (options.onFailFunction) this.onFailFunction = options.onFailFunction;
        if (options.parentView) this.parentView = options.parentView;
    },
    onRender: function () {
        let self = this;
        let widgetOptions = {
            dataType: 'json',
            dropZone: this.$('#dropzone'),
            autoUpload: this.autoUpload,
            headers: {'X-CSRF-Token': `${appConfig().csrf}` },
            method: 'PUT',
            multipart: false
        };

        if (!this.options.autoUpload) {
            widgetOptions.add = function (e, data) {
                if (!!self.parentView) {
                    self.parentView.off('submit:logo').on('submit:logo', function (id) {
                        data.url = self.options.fileUploadUrl(id);
                        data.submit();
                    });
                }
            }
        }

        this.$('#fileupload').fileupload(widgetOptions);

        this.$('#fileupload')
            .bind('fileuploaddone', jQuery.proxy(this.onDone, this))
            .bind('fileuploadfail', jQuery.proxy(this.onFail, this))
            .bind('fileuploadprogress', jQuery.proxy(this.onProgress, this))
            .bind('fileuploadprogressall', jQuery.proxy(this.onProgressAll, this))
            .bind('fileuploadadd', jQuery.proxy(this.onAdd, this));

        //
        //// TODO: for testing with CORS on IE only, remove in production
        //this.$('#fileupload').fileupload(
        //    'option',
        //    'redirect',
        //    window.location.href.replace(
        //        /\/[^\/]*$/,
        //        '/cors/result.html?%s'
        //    )
        //);

    },
    onAdd: function (e, data) {
        this.amount += 1;
        let ProgressModel = Backbone.Model.extend({
            defaults: {
                filename: data.files[0].name,
                progress: 0,
                bitrate: '',
                remaining: '',
                fileSize: 0,
                uploadedBytes: 0
            }
        });
        data.itemModel = new ProgressModel();
        data.itemView = new this.progressView({model: data.itemModel});
        if (!!this.parentView) {
            data.url = this.options.fileUploadUrl(this.parentView.model.get('id'));
        }
        this.$el.parent().append(data.itemView.render().$el);
        this.$('.dropzone-wrapper').hide();
        if (this.amount > 1) this.$('.progress').removeClass('hidden');

        this.trigger('fileuploadadd', data.files[0], data);
    },
    onDone: function (e, data) {
        data.itemView.hideInfo();

        this.trigger('itemDone', data.result, data.itemView, data.itemModel);

        this.amount -= 1;
        if (this.amount === 0) {
            this.$('.progress').addClass('hidden');
            this.trigger('fileuploaddone',  data.files[0], data);
        }
    },
    onFail: function (e, data) {
        if (_.isFunction(this.onFailFunction))
            this.onFailFunction(e, data);
    },
    onProgress: function (e, data) {
        let progress = ~~(data.loaded / data.total * 100);
        let remaining = (data.total - data.loaded) * 8 / data.bitrate;
        data.itemModel.set({
            'progress': progress,
            'bitrate': this._formatBitrate(data.bitrate),
            'remaining': this._formatTime(remaining),
            'fileSize': this._formatFileSize(data.total),
            'uploadedBytes': this._formatFileSize(data.loaded)
        });
    },
    onProgressAll: function (e, data) {
        let progress = ~~(data.loaded / data.total * 100);
        this.updateOverallProgress(progress);
    },
    updateOverallProgress: function (value) {
        let percentage = value + '%';
        this.$('.progress-bar').css('width', percentage).html(percentage);
    },
    _formatTime: function (seconds) {
        let date = new Date(seconds * 1000),
            days = Math.floor(seconds / 86400);
        days = days ? days + 'd ' : '';
        return days +
            ('0' + date.getUTCHours()).slice(-2) + ':' +
            ('0' + date.getUTCMinutes()).slice(-2) + ':' +
            ('0' + date.getUTCSeconds()).slice(-2);
    },
    _formatFileSize: function (bytes) {
        if (typeof bytes !== 'number') {
            return '';
        }
        if (bytes >= 1000000000) {
            return (bytes / 1000000000).toFixed(2) + ' GB';
        }
        if (bytes >= 1000000) {
            return (bytes / 1000000).toFixed(2) + ' MB';
        }
        return (bytes / 1000).toFixed(2) + ' KB';
    },
    _formatBitrate: function (bits) {
        if (typeof bits !== 'number') {
            return '';
        }
        if (bits >= 1000000000) {
            return (bits / 1000000000).toFixed(2) + ' Gbit/s';
        }
        if (bits >= 1000000) {
            return (bits / 1000000).toFixed(2) + ' Mbit/s';
        }
        if (bits >= 1000) {
            return (bits / 1000).toFixed(2) + ' kbit/s';
        }
        return bits.toFixed(2) + ' bit/s';
    }
});

export default FileUploadView;
import FileUploadView from "views/file-upload/file-upload";

import Radio from 'backbone.radio';

const modalChannel = Radio.channel('modal');

class FileUploaderService extends Marionette.Object {

    upload(options, view) {
        let that = this;

        let uploadView = new FileUploadView({
            autoUpload: options.autoUpload || false,
            message: options.message || 'Message about file',
            fileUploadUrl: view.getImageUploadUrl,
            fileAttribute: options.fileAttribute || 'logo',
            acceptedFileTypes: options.acceptedFileTypes || 'image/*',
            parentView: view
        });

        if (options.appendTo) {
            view.$(options.appendTo).html(uploadView.render().$el);
        }
        else {
            modalChannel.trigger('open:as:modal', uploadView, {
                header: options.header || 'File Upload'
            });
        }

        if (!options.autoUpload) {
            uploadView.on('fileuploadadd', function (data) {
                $('img')
                    .on('error', function () {
                        console.log("error loading image");
                    });
                let blob = that.createObjectURL(data);
                view.triggerMethod('fileAdded', {src: blob, name: data.name});
                modalChannel.trigger('close', uploadView)
            });
        }
        else {
            uploadView.on('fileuploaddone', function (e, data) {
                view.triggerMethod('fileUploaded', data.result.logoUrl);
                if (options.appendTo) {
                    view.$(options.appendTo).html(uploadView.render().$el);
                }
                else {
                    modalChannel.trigger('close', uploadView);
                }
            });

        }
    }

    createObjectURL(file) {
        if (window.URL && window.URL.createObjectURL) {
            return window.URL.createObjectURL(file);
        } else if (window.webkitURL) {
            return window.webkitURL.createObjectURL(file);
        } else {
            return null;
        }
    }

    uploadBadges(e, url) {
        let data = e.data;

        let byteString = data.split(",")[1].split(';')[0];
        let contentType = data.split(':')[1].split(";")[0];

        let binary_string = window.atob(byteString);
        let len = binary_string.length;
        let bytes = new Uint8Array(len);

        for (let i = 0; i < len; i++) {
            bytes[i] = binary_string.charCodeAt(i);
        }

        return $.ajax({
            url: url,
            contentType: contentType,
            type: 'PUT',
            data: bytes,
            headers: {
                'X-CSRF-Token': Liferay.authToken,
                'Content-Disposition': 'attachment; filename="icon.png"'
            },
            processData: false
        });
    };
}

export default FileUploaderService
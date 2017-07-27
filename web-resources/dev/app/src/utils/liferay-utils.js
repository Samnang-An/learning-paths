
class LiferayUtil {
    static getUserLocale() {
        let language = Liferay.ThemeDisplay.getLanguageId();
        let index = language.indexOf('_');
        if (index > 0) {
            language = language.substr(0, index);
        }
        return language;
    }

    static getUserLanguageId() {
        return Liferay.ThemeDisplay.getLanguageId();
    }

    static authToken() {
        return  Liferay.authToken;
    }

    static courseId() {
        return parseInt(Liferay.ThemeDisplay.getScopeGroupId());
    }

    static plId() {
        return parseInt(Liferay.ThemeDisplay.getPlid());
    }

    static userId() {
        return parseInt(Liferay.ThemeDisplay.getUserId());
    }

    static pathMain() {
        return Liferay.ThemeDisplay.getPathMain();
    }
}

export default LiferayUtil;

export const get = (key, defValue) => {
    try {
        const val = localStorage.getItem(key);
        return val ? JSON.parse(val) : defValue;
    } catch {
        return null
    }
};

export const set = (key, value) => {
    localStorage.setItem(key, JSON.stringify(value));
};

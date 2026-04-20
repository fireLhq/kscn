import axios from "axios";
import store from "@/store";

export function applyTokenHeader(config = {}, token = null) {
    if (!token) return config;
    config.headers = config.headers || {};
    if (!config.headers.Authorization) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}

function applyInterceptors(instance) {
    instance.interceptors.request.use(
        (config) => {
            const token = store.state.token || localStorage.getItem("jwt_token");
            return applyTokenHeader(config, token);
        },
        (error) => Promise.reject(error)
    );

    instance.interceptors.response.use(
        (response) => response,
        (error) => {
            if (error.response) {
                const status = error.response.status;
                const message = error.response.data?.message;

                if (status === 401) {
                    store.commit("clearToken");
                } else if (status === 403) {
                    console.warn("权限不足：", message);
                }
            }
            return Promise.reject(error);
        }
    );

    return instance;
}

export function createApiClient(baseURL, timeout = 5000) {
    return applyInterceptors(
        axios.create({
            baseURL,
            timeout,
        })
    );
}

const request = createApiClient("/api", 300000);

export default request;

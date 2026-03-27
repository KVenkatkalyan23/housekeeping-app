import {
  fetchBaseQuery,
  type BaseQueryFn,
  type FetchArgs,
  type FetchBaseQueryError,
} from "@reduxjs/toolkit/query/react";

import type { RootState } from "../../app/store";
import {
  clearPersistedAuthState,
  logout,
  readPersistedAuthState,
} from "../../features/auth/slice";
import { baseUrl } from "../constants";

const rawBaseQuery = fetchBaseQuery({
  baseUrl,
  prepareHeaders: (headers, { getState }) => {
    const state = getState() as RootState;
    const persistedAuth = readPersistedAuthState();
    const accessToken = state.auth.accessToken ?? persistedAuth?.accessToken;

    if (accessToken) {
      headers.set("Authorization", `Bearer ${accessToken}`);
    }

    return headers;
  },
});

export const baseQueryWithAuth: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions) => {
  const result = await rawBaseQuery(args, api, extraOptions);

  if (result.error?.status === 401) {
    clearPersistedAuthState();
    api.dispatch({ type: "baseApi/resetApiState" });
    api.dispatch(logout());

    if (window.location.pathname !== "/login") {
      window.location.assign("/login");
    }
  }

  return result;
};

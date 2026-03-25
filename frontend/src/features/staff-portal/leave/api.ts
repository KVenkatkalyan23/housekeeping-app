import { baseApi } from "../../../shared/api/baseApi";
import type { LeaveHistoryItemResponse } from "./types";

export const leaveApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getLeaveHistory: builder.query<LeaveHistoryItemResponse[], void>({
      query: () => ({
        url: "/leave/history",
        method: "GET",
      }),
    }),
  }),
});

export const { useGetLeaveHistoryQuery } = leaveApi;

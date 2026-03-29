import { baseApi } from '../../../shared/api/baseApi'
import type {
  AdminAllocatedTaskListResponse,
  ManualTaskReassignmentRequest,
  ManualTaskReassignmentResponse,
  ReassignmentCandidateItem,
  AdminTaskAllocationSummary,
  AdminTaskStatusFilter,
  AdminTaskTypeFilter,
} from './types'

export interface GetAdminTaskAllocationListParams {
  taskDate?: string
  page: number
  size: number
  search?: string
  taskType: AdminTaskTypeFilter
  status: AdminTaskStatusFilter
}

export const adminTaskAllocationApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAdminTaskAllocationSummary: builder.query<
      AdminTaskAllocationSummary,
      { taskDate?: string } | void
    >({
      query: (arg) => ({
        url: '/admin/tasks/allocation/summary',
        method: 'GET',
        params: arg?.taskDate ? { taskDate: arg.taskDate } : undefined,
      }),
      providesTags: ['AdminTaskAllocation'],
    }),
    getAdminTaskAllocationList: builder.query<
      AdminAllocatedTaskListResponse,
      GetAdminTaskAllocationListParams
    >({
      query: ({ taskDate, page, size, search, taskType, status }) => ({
        url: '/admin/tasks/allocation',
        method: 'GET',
        params: {
          page,
          size,
          ...(taskDate ? { taskDate } : {}),
          ...(search ? { search } : {}),
          ...(taskType !== 'ALL' ? { taskType } : {}),
          ...(status !== 'ALL' ? { status } : {}),
        },
      }),
      providesTags: ['AdminTaskAllocation'],
    }),
    getTaskReassignmentCandidates: builder.query<
      ReassignmentCandidateItem[],
      { taskId: string }
    >({
      query: ({ taskId }) => ({
        url: '/admin/tasks/reassign/candidates',
        method: 'GET',
        params: { taskId },
      }),
    }),
    reassignTask: builder.mutation<
      ManualTaskReassignmentResponse,
      ManualTaskReassignmentRequest
    >({
      query: (body) => ({
        url: '/admin/tasks/reassign',
        method: 'POST',
        body,
      }),
      invalidatesTags: ['AdminTaskAllocation'],
    }),
  }),
})

export const {
  useGetAdminTaskAllocationSummaryQuery,
  useGetAdminTaskAllocationListQuery,
  useGetTaskReassignmentCandidatesQuery,
  useReassignTaskMutation,
} = adminTaskAllocationApi

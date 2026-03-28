import { createBrowserRouter, Navigate } from "react-router-dom";

import { AdminDashboardPage } from "../../features/admin-portal/dashboard/pages/AdminDashboardPage";
import { AdminStaffDirectoryPage } from "../../features/admin-portal/staff-directory/pages/AdminStaffDirectoryPage";
import { ProtectedRoute } from "../../features/auth/components/ProtectedRoute";
import { LoginPage } from "../../features/auth/pages/LoginPage";
import { AttendanceHistoryPage } from "../../features/staff-portal/attendance/pages/AttendanceHistoryPage";
import { StaffAttendancePage } from "../../features/staff-portal/attendance/pages/StaffAttendancePage";
import { LeaveHistoryPage } from "../../features/staff-portal/leave/pages/LeaveHistoryPage";
import { ProfilePage } from "../../features/staff-portal/profile/pages/ProfilePage";
import { ErrorPage } from "./ErrorPage";
import { RoleHomeRedirect } from "./RoleHomeRedirect";

export const router = createBrowserRouter([
  {
    path: "/login",
    element: <LoginPage />,
    errorElement: <ErrorPage />,
  },
  {
    path: "/",
    element: <ProtectedRoute />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <RoleHomeRedirect />,
      },
    ],
  },
  {
    path: "/admin",
    element: <ProtectedRoute allowedRoles={["ADMIN"]} />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <Navigate to="/admin/dashboard" replace />,
      },
      {
        path: "dashboard",
        element: <AdminDashboardPage />,
      },
      {
        path: "staff",
        element: <AdminStaffDirectoryPage />,
      },
    ],
  },
  {
    path: "/staff",
    element: <ProtectedRoute allowedRoles={["STAFF"]} />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <RoleHomeRedirect />,
      },
      {
        path: "attendance",
        element: <StaffAttendancePage />,
      },
      {
        path: "attendance/history",
        element: <AttendanceHistoryPage />,
      },
      {
        path: "leave",
        element: <LeaveHistoryPage />,
      },
      {
        path: "profile",
        element: <ProfilePage />,
      },
    ],
  },
]);

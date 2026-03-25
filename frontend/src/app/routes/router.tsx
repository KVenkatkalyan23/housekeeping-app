import { useSelector } from "react-redux";
import { createBrowserRouter, Navigate } from "react-router-dom";

import { ProtectedRoute } from "../../features/auth/components/ProtectedRoute";
import { LoginPage } from "../../features/auth/pages/LoginPage";
import { getDefaultRouteForRole } from "../../features/auth/routing";
import { HomePage } from "../../features/dashboard/pages/HomePage";
import { AttendanceHistoryPage } from "../../features/staff-portal/attendance/pages/AttendanceHistoryPage";
import { StaffAttendancePage } from "../../features/staff-portal/attendance/pages/StaffAttendancePage";
import { LeaveHistoryPage } from "../../features/staff-portal/leave/pages/LeaveHistoryPage";
import { ProfilePage } from "../../features/staff-portal/profile/pages/ProfilePage";
import type { RootState } from "../store";
import { ErrorPage } from "./ErrorPage";

function RoleHomeRedirect() {
  const role = useSelector((state: RootState) => state.auth.role);
  return <Navigate to={getDefaultRouteForRole(role)} replace />;
}

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
        element: <HomePage />,
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

import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { DifficultyBadge } from '../components/Badges';
import { Edit3, X, Flame, CheckCircle2, Target, Award, AlertTriangle, ArrowRight, BookOpen, Calendar } from 'lucide-react';

export const ProfilePage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [titleTagline, setTitleTagline] = useState('Computer Science Student');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    fetchProfile();
    const savedTagline = localStorage.getItem(`codeforge_tagline_${user?.id}`);
    if (savedTagline) setTitleTagline(savedTagline);
  }, [user]);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const res = await api.get('/users/me/stats');
      const data = res.data.data;
      setProfile(data);
      setAvatarUrl(data?.avatarUrl || '');
    } catch (err) {
      console.error('Failed to load profile', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setUpdating(true);
    try {
      if (titleTagline) {
        localStorage.setItem(`codeforge_tagline_${user?.id}`, titleTagline);
      }
      const res = await api.put('/users/me', { avatarUrl });
      setProfile(res.data.data);
      setShowEditModal(false);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setUpdating(false);
    }
  };

  const initials = user?.username ? user.username.substring(0, 2).toUpperCase() : 'JD';

  const easy = profile?.easySolved ?? 0;
  const medium = profile?.mediumSolved ?? 0;
  const hard = profile?.hardSolved ?? 0;
  const totalSolved = profile?.solvedCount ?? 0;
  const totalSubs = profile?.totalSubmissions ?? 0;
  const acceptedSubs = profile?.acceptedSubmissions ?? 0;
  const accRate = profile?.acceptanceRate ?? 0.0;
  const streak = profile?.streak ?? 0;
  const topicStats = profile?.topicStats || [];
  const weakTopics = profile?.weakTopics || [];
  const recommendedProblems = profile?.recommendedProblems || [];
  const dailyActivity = profile?.dailyActivity || {};
  const activeDaysCount = Object.keys(dailyActivity).length;

  const maxCategory = Math.max(easy, medium, hard, 5);

  if (loading && !profile) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 font-sans space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">User Profile</h1>
        <p className="text-xs sm:text-sm text-gray-400 mt-1">Track your progress, topic mastery, and recommendations</p>
      </div>

      {/* Main Profile Card */}
      <div className="p-6 sm:p-8 rounded-2xl bg-[#0F172A]/90 border border-gray-800 shadow-2xl space-y-8">
        
        {/* Top: Avatar, Name, Handle, Tagline, Edit Profile */}
        <div className="flex flex-col sm:flex-row items-center sm:items-start justify-between gap-6 pb-6 border-b border-gray-800/80">
          <div className="flex flex-col sm:flex-row items-center gap-5 text-center sm:text-left">
            {profile?.avatarUrl ? (
              <img
                src={profile.avatarUrl}
                alt={user?.username}
                className="w-20 h-20 rounded-full object-cover border-2 border-blue-500/40 shadow-lg"
              />
            ) : (
              <div className="w-20 h-20 rounded-full bg-[#1E293B] border-2 border-gray-700 text-2xl font-bold text-gray-200 flex items-center justify-center shadow-lg font-mono">
                {initials}
              </div>
            )}

            <div className="space-y-1">
              <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                {user?.username ? user.username.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase()) : 'Contestant'}
              </h2>
              <p className="text-xs text-gray-400 font-mono">
                @{user?.username?.toLowerCase() || 'user'} • {profile?.role?.replace('ROLE_', '') || 'PARTICIPANT'}
              </p>
              <p className="text-xs text-gray-300 font-sans pt-0.5">
                {titleTagline || 'Competitive Programmer'}
              </p>
            </div>
          </div>

          <button
            onClick={() => setShowEditModal(true)}
            className="px-4 py-2 rounded-lg bg-gray-900 hover:bg-gray-800 border border-gray-700 text-xs font-semibold text-gray-200 flex items-center gap-2 transition-colors"
          >
            <Edit3 className="w-3.5 h-3.5 text-gray-400" />
            <span>Edit Profile</span>
          </button>
        </div>

        {/* 4 Core Stat Cards */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
          <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800/80">
            <span className="text-2xl sm:text-3xl font-extrabold text-blue-400 font-mono block">
              {totalSolved}
            </span>
            <span className="text-xs text-gray-400 font-medium block mt-1">
              Problems Solved
            </span>
          </div>

          <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800/80">
            <span className="text-2xl sm:text-3xl font-extrabold text-emerald-400 font-mono block">
              {acceptedSubs}
            </span>
            <span className="text-xs text-gray-400 font-medium block mt-1">
              Accepted Submissions
            </span>
          </div>

          <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800/80">
            <span className="text-2xl sm:text-3xl font-extrabold text-white font-mono block">
              {totalSubs}
            </span>
            <span className="text-xs text-gray-400 font-medium block mt-1">
              Total Subs ({accRate}%)
            </span>
          </div>

          <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800/80">
            <div className="flex items-center justify-center gap-1.5">
              <Flame className={`w-5 h-5 ${streak > 0 ? 'text-amber-500 fill-amber-500' : 'text-gray-500'}`} />
              <span className="text-2xl sm:text-3xl font-extrabold text-amber-400 font-mono">
                {streak}
              </span>
            </div>
            <span className="text-xs text-gray-400 font-medium block mt-1">
              Day Streak ({activeDaysCount} active days)
            </span>
          </div>
        </div>

        {/* Difficulty Breakdown Progress Bars */}
        <div className="space-y-4 pt-2">
          <h3 className="text-sm font-semibold text-gray-300 uppercase tracking-wider">Difficulty Breakdown</h3>
          <div className="space-y-3">
            {/* Easy */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between text-xs font-semibold">
                <span className="text-emerald-400 flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block"></span>
                  Easy
                </span>
                <span className="font-mono text-gray-300 font-bold">{easy} solved</span>
              </div>
              <div className="w-full h-2 rounded-full bg-gray-900 overflow-hidden border border-gray-800">
                <div
                  className="h-full rounded-full bg-emerald-500 transition-all duration-500"
                  style={{ width: `${Math.min(100, (easy / maxCategory) * 100)}%` }}
                ></div>
              </div>
            </div>

            {/* Medium */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between text-xs font-semibold">
                <span className="text-amber-400 flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-500 inline-block"></span>
                  Medium
                </span>
                <span className="font-mono text-gray-300 font-bold">{medium} solved</span>
              </div>
              <div className="w-full h-2 rounded-full bg-gray-900 overflow-hidden border border-gray-800">
                <div
                  className="h-full rounded-full bg-amber-500 transition-all duration-500"
                  style={{ width: `${Math.min(100, (medium / maxCategory) * 100)}%` }}
                ></div>
              </div>
            </div>

            {/* Hard */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between text-xs font-semibold">
                <span className="text-rose-400 flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-rose-500 inline-block"></span>
                  Hard
                </span>
                <span className="font-mono text-gray-300 font-bold">{hard} solved</span>
              </div>
              <div className="w-full h-2 rounded-full bg-gray-900 overflow-hidden border border-gray-800">
                <div
                  className="h-full rounded-full bg-rose-500 transition-all duration-500"
                  style={{ width: `${Math.min(100, (hard / maxCategory) * 100)}%` }}
                ></div>
              </div>
            </div>
          </div>
        </div>

      </div>

      {/* Recommended Next Problems & Weak Topics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* Recommended Problems (2 cols on md) */}
        <div className="md:col-span-2 p-6 rounded-2xl bg-[#0F172A]/90 border border-gray-800 shadow-xl space-y-4">
          <div className="flex items-center justify-between border-b border-gray-800 pb-3">
            <div className="flex items-center gap-2">
              <Target className="w-5 h-5 text-blue-400" />
              <h3 className="text-base font-bold text-white">Recommended Problems For You</h3>
            </div>
            <Link to="/problems" className="text-xs text-blue-400 hover:text-blue-300 flex items-center gap-1">
              <span>View all</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          {recommendedProblems.length > 0 ? (
            <div className="space-y-3">
              {recommendedProblems.map((prob) => (
                <Link
                  key={prob.id}
                  to={`/problems/${prob.slug}`}
                  className="block p-3.5 rounded-xl bg-gray-900/60 hover:bg-gray-800/80 border border-gray-800/80 hover:border-gray-700 transition-all group"
                >
                  <div className="flex items-center justify-between">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2.5">
                        <span className="font-semibold text-sm text-gray-200 group-hover:text-blue-400 transition-colors">
                          {prob.title}
                        </span>
                        <DifficultyBadge difficulty={prob.difficulty} />
                      </div>
                      <div className="flex items-center gap-2 text-xs text-gray-400">
                        <span>Acceptance: {prob.acceptanceRate}%</span>
                        {prob.topics && prob.topics.length > 0 && (
                          <span>• {prob.topics.join(', ')}</span>
                        )}
                      </div>
                    </div>
                    <ArrowRight className="w-4 h-4 text-gray-500 group-hover:text-blue-400 group-hover:translate-x-1 transition-all" />
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="p-8 text-center text-gray-400 text-xs">
              <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
              <span>Great job! You have solved all current recommended problems. Explore the problem repository for more challenges.</span>
            </div>
          )}
        </div>

        {/* Weak Topics / Areas for Practice */}
        <div className="p-6 rounded-2xl bg-[#0F172A]/90 border border-gray-800 shadow-xl space-y-4">
          <div className="flex items-center gap-2 border-b border-gray-800 pb-3">
            <AlertTriangle className="w-5 h-5 text-amber-400" />
            <h3 className="text-base font-bold text-white">Areas to Practice</h3>
          </div>

          {weakTopics.length > 0 ? (
            <div className="space-y-2">
              <p className="text-xs text-gray-400">
                Topics with low accuracy (&lt;50%) or pending solutions:
              </p>
              <div className="flex flex-wrap gap-2 pt-1">
                {weakTopics.map((topic) => (
                  <span
                    key={topic}
                    className="px-2.5 py-1 rounded-lg text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/30"
                  >
                    {topic}
                  </span>
                ))}
              </div>
            </div>
          ) : (
            <div className="p-6 text-center text-gray-400 text-xs space-y-2">
              <Award className="w-8 h-8 text-amber-400 mx-auto" />
              <p className="text-gray-300 font-medium">All Topics Strong</p>
              <p className="text-gray-500">No weak topics identified (&gt;50% accuracy on all attempted topics).</p>
            </div>
          )}

          {/* Topic-wise accuracy list */}
          {topicStats.length > 0 && (
            <div className="pt-2 border-t border-gray-800 space-y-2">
              <h4 className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Topic Accuracy</h4>
              <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                {topicStats.map((stat) => (
                  <div key={stat.topic} className="text-xs space-y-1">
                    <div className="flex justify-between text-gray-300">
                      <span>{stat.topic}</span>
                      <span className="font-mono text-gray-400">{stat.accuracy}% ({stat.uniqueSolved} solved)</span>
                    </div>
                    <div className="w-full h-1.5 rounded-full bg-gray-900 overflow-hidden">
                      <div
                        className={`h-full rounded-full ${stat.accuracy >= 70 ? 'bg-emerald-500' : stat.accuracy >= 50 ? 'bg-amber-500' : 'bg-rose-500'}`}
                        style={{ width: `${Math.min(100, stat.accuracy)}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

      </div>

      {/* Edit Profile Modal */}
      {showEditModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4">
          <div className="w-full max-w-md bg-[#0D1117] border border-gray-800 rounded-2xl p-6 shadow-2xl relative">
            <button
              onClick={() => setShowEditModal(false)}
              className="absolute top-4 right-4 text-gray-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-lg font-bold text-white mb-1">Edit Profile</h3>
            <p className="text-xs text-gray-400 mb-5">Update your tagline and avatar image URL</p>

            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">Title / Student Tagline</label>
                <input
                  type="text"
                  value={titleTagline}
                  onChange={(e) => setTitleTagline(e.target.value)}
                  placeholder="e.g. Computer Science Student"
                  className="w-full px-3.5 py-2 bg-gray-900 border border-gray-800 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">Avatar Image URL</label>
                <input
                  type="url"
                  value={avatarUrl}
                  onChange={(e) => setAvatarUrl(e.target.value)}
                  placeholder="https://... or leave empty to use initials circle"
                  className="w-full px-3.5 py-2 bg-gray-900 border border-gray-800 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="px-4 py-2 rounded-xl bg-gray-900 text-gray-300 text-xs font-semibold hover:bg-gray-800"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={updating}
                  className="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold shadow-md disabled:opacity-50"
                >
                  {updating ? 'Saving...' : 'Save Profile'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

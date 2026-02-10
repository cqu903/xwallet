import 'dart:async';

import 'package:flutter/material.dart';

import '../../analytics/event_spec.dart';
import '../../models/analytics_event.dart';
import '../../services/analytics_service.dart';
import 'analytics_tap.dart';

class TrackedTabItem {
  final IconData icon;
  final String label;
  final String elementId;
  final AnalyticsPage page;
  final String? flow;

  const TrackedTabItem({
    required this.icon,
    required this.label,
    required this.elementId,
    required this.page,
    this.flow,
  });
}

class TrackedBottomNavBar extends StatefulWidget {
  final int currentIndex;
  final List<TrackedTabItem> items;
  final ValueChanged<int> onTap;
  final String pageViewEntry;
  final String clickEntry;

  const TrackedBottomNavBar({
    super.key,
    required this.currentIndex,
    required this.items,
    required this.onTap,
    this.pageViewEntry = 'tab',
    this.clickEntry = 'tab',
  });

  @override
  State<TrackedBottomNavBar> createState() => _TrackedBottomNavBarState();
}

class _TrackedBottomNavBarState extends State<TrackedBottomNavBar> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _trackPageView(widget.currentIndex);
    });
  }

  @override
  void didUpdateWidget(covariant TrackedBottomNavBar oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.currentIndex != widget.currentIndex) {
      _trackPageView(widget.currentIndex);
    }
  }

  void _trackPageView(int index) {
    if (index < 0 || index >= widget.items.length) {
      return;
    }

    final item = widget.items[index];

    unawaited(
      AnalyticsService.instance
          .trackStandardEvent(
            eventType: AnalyticsEventType.pageView,
            properties: AnalyticsEventProperties.pageView(
              page: item.page,
              flow: item.flow,
              entry: widget.pageViewEntry,
            ),
            category: EventCategory.behavior,
          )
          .catchError((_) {}),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Colors.white, Color(0xFFFDFCFF)],
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF000000).withOpacity(0.08),
            blurRadius: 20,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.only(top: 12, bottom: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: List.generate(widget.items.length, (index) {
              final isSelected = widget.currentIndex == index;
              final item = widget.items[index];

              return AnalyticsTap(
                eventType: AnalyticsEventType.tabClick,
                properties: AnalyticsEventProperties.click(
                  page: AnalyticsPages.mainNavigation,
                  flow: item.flow,
                  entry: widget.clickEntry,
                  elementId: item.elementId,
                  elementType: AnalyticsElementType.tab,
                  elementText: item.label,
                ),
                category: EventCategory.behavior,
                onTap: () => widget.onTap(index),
                child: SizedBox(
                  width: 80,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        item.icon,
                        size: 24,
                        color: isSelected
                            ? const Color(0xFF7424F5)
                            : const Color(0xFF666666),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        item.label,
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight:
                              isSelected ? FontWeight.w600 : FontWeight.w500,
                          color: isSelected
                              ? const Color(0xFF7424F5)
                              : const Color(0xFF666666),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            }),
          ),
        ),
      ),
    );
  }
}

//
//  NotificationView.swift
//  Read
//
//  Created by Christian  on 2/13/24.
//

import SwiftUI

struct NotificationView: View {
    var message: String
    
    var body: some View {
        Text(message)
            .font(.headline)
            .foregroundColor(.white)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(Color.teal)
            .cornerRadius(10)
            .transition(.opacity)
    }
}


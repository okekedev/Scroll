//
//  SplashScreenView.swift
//  Read
//
//  Created by Christian  on 2/13/24.
//

import SwiftUI

struct SplashScreenView: View {
    var body: some View {
        ZStack {
            Color.teal
                .ignoresSafeArea()

            Image("AppIcon")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .padding()
        }
    }
}

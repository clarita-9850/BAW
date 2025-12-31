'use client';

import React from 'react';
import styles from './CardGrid.module.css';

export interface CardItem {
  type: string;
  bodyText: string;
  icon?: string;
  titleText: string;
  imageSrc?: string;
  buttonText?: string;
  buttonHref?: string;
}

interface CardGridProps {
  contentArr: CardItem[];
}

export default function CardGrid({ contentArr }: CardGridProps) {
  return (
    <div className={styles.cardGrid}>
      <div className="row">
        {contentArr.map((card, index) => (
          <div key={index} className="col-md-4 mb-4">
            <div className={`card ${styles.card} h-100`}>
              <div className={`card-body ${styles.cardBody} text-center`}>
                {card.type === 'icon' && card.icon && (
                  <div className={styles.cardIcon}>
                    <i className={card.icon}></i>
                  </div>
                )}
                {card.imageSrc && (
                  <img
                    src={card.imageSrc}
                    alt={card.titleText}
                    className={`card-img-top ${styles.cardImgTop} mb-3`}
                  />
                )}
                <h5 className={`card-title ${styles.cardTitle}`}>{card.titleText}</h5>
                <p className={`card-text ${styles.cardText}`}>{card.bodyText}</p>
                {card.buttonText && card.buttonHref && (
                  <a href={card.buttonHref} className="btn btn-primary">
                    {card.buttonText}
                  </a>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

